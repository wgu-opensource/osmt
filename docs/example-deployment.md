# Example deployment: split public and staff instances on AWS

This document describes a reference deployment of OSMT on AWS for an
institution that wants a **public, read-only skills catalog** and a **private
authoring site** for staff, each with its own branding.

It is written for a site reliability engineer who will implement the
deployment with their own infrastructure-as-code. It covers the decisions that
are specific to OSMT — profiles, configuration, database requirements, search,
identity — and deliberately leaves out generic AWS setup that your
organization already has patterns for. There is no Terraform or Helm here; the
intent is that you can read this once and then write your own.

The example uses Amazon EKS. Nothing in OSMT requires Kubernetes — the same
configuration works on ECS or plain containers — but the reference topology
below assumes EKS because that is the most common institutional starting point.

Related reading:

- [Split deployment](features/2026-04-13-split-deployment.md)
- [White-label & theming](features/2026-03-19-whitelabel-theming.md)
- [Authentication](features/2026-02-28-auth.md)

## The two-instance model

OSMT is deployed twice from the same image, with different configuration.

**The public instance** serves the read-only catalog at a public hostname.
Anyone can browse skills and collections and share links. There is no login.

**The staff instance** serves the authoring application at a separate hostname
behind your VPN. Staff sign in with the institutional identity provider and
create, edit, and publish content.

The behavior difference comes from one Spring profile. Activating `readonly`
applies `application-readonly.properties`, which sets:

```properties
spring.flyway.enabled=false
spring.session.store-type=none

app.readOnlyMode=true
app.singleAuthEnabled=false
app.authMode=read-only
app.enableRoles=false

app.allowPublicSearching=true
app.allowPublicLists=true
```

So the public instance stores no sessions, exposes no login, and permits public
search and list browsing. Requests to mutating or authenticated-only routes
return **403**.

### The boundary, stated precisely

The read-only guarantee rests on three independent layers:

1. **Application** — the `readonly` profile activates `ReadOnlySecurityConfig`,
   which permits only safe public routes.
2. **Database** — the public instance connects as a user holding only `SELECT`.
3. **Network** — in this architecture the two instances sit behind different
   load balancers, and the authoring one is not reachable from the internet.

The first layer has a sharp edge worth knowing before you configure anything.
The three security configurations are mutually exclusive by profile:

```kotlin
@Profile("readonly & !oauth2")              // ReadOnlySecurityConfig
@Profile("oauth2 & !readonly")              // SecurityConfig
@Profile("single-auth & !oauth2 & !readonly") // SingleAuthSecurityConfig
```

If OAuth environment variables reach the public instance, the entrypoint
appends the `oauth2` profile and **none of the three applies**. The pod then
either fails to start — the OAuth session-token configuration requires
`APP_SESSION_TOKEN_SECRET`, which you would not have set there — or falls back
to Spring Boot's default chain, which demands authentication for every request.
The public site breaks loudly rather than leaking write access, so this fails
closed, but it is still the easiest mistake to make in this deployment.

Keep **all** OAuth configuration off the public instance. That means more than
the four PingFederate variables: `OAUTH_GOOGLE_CLIENT_ID` and
`OAUTH_GOOGLE_CLIENT_SECRET` together are also enough to activate the `oauth2`
profile.

## Reference architecture

```
                      Internet                  Institutional VPN
                          │                             │
                          ▼                             ▼
              ┌───────────────────────┐   ┌───────────────────────┐
              │  Public ALB           │   │  Internal ALB         │
              │  internet-facing      │   │  SG: VPN CIDRs only   │
              │  osmt.example.edu     │   │  osmt-admin.example.edu│
              └───────────┬───────────┘   └───────────┬───────────┘
                          │ :8080                     │ :8080
    ┌─────────────────────┼───────────────────────────┼──────────────────┐
    │  EKS cluster        ▼                           ▼                  │
    │             ┌──────────────┐            ┌──────────────┐           │
    │             │ osmt-public  │            │ osmt-staff   │           │
    │             │ Deployment   │            │ Deployment   │           │
    │             │ 2 replicas   │            │ 1–2 replicas │           │
    │             │ profile:     │            │ profile:     │           │
    │             │  readonly    │            │  oauth2      │           │
    │             │ SA: ui-only  │            │ SA: read-write│          │
    │             └──────┬───────┘            └──────┬───────┘           │
    │                    │                           │                   │
    │             ┌──────┴───────────────────────────┴───────┐           │
    │             │  Elasticsearch 8.x (ECK operator)        │           │
    │             │  1–2 nodes, gp3                          │           │
    │             └──────────────────────────────────────────┘           │
    └────────────────────┬───────────────────────────┬──────────────────┘
                         │                           │
              ┌──────────▼──────────┐     ┌──────────▼──────────┐
              │ Aurora MySQL 8.0    │     │ ElastiCache Redis   │
              │ reader ← public     │     │ single small node   │
              │ writer ← staff      │     │                     │
              └─────────────────────┘     └─────────────────────┘

  Secrets Manager — DB credentials      SSM Parameter Store — config
  KMS — encryption for both             CloudWatch — logs and metrics
```

### Networking

A VPC with public and private subnets across three availability zones, an
internet gateway, and a single NAT gateway. The NAT gateway is shared; at this
traffic level per-AZ NAT gateways are not worth their cost.

Load balancers sit in the public subnets. Everything else — EKS nodes, Aurora,
Redis, Elasticsearch — sits in private subnets.

### Two deployments, not one

Run the public and staff instances as **separate Kubernetes Deployments**. Both
listen on port **8080**; because they are separate pods there is no port
conflict and no need to override the server port.

| | `osmt-public` | `osmt-staff` |
|---|---|---|
| Replicas | 2 | 1–2 |
| PodDisruptionBudget | `minAvailable: 1` | `minAvailable: 1` |
| ServiceAccount | `osmt-ui-only-sa` | `osmt-read-write-sa` |
| Exposed via | internet-facing ALB | internal ALB |

Give each Deployment its own ServiceAccount bound to its own IAM role via IRSA,
and scope each role to only the secrets that instance needs. This is what makes
the split meaningful: the public pods cannot read the read-write database
credentials even if the application layer is compromised.

### Load balancers and DNS

Two ALBs, because they have different exposure:

- **Public ALB** — internet-facing, HTTPS on 443, security group allowing 443
  from anywhere, forwarding to the `osmt-public` service on 8080.
- **Internal ALB** — internal scheme, HTTPS on 443, security group allowing 443
  **only from your VPN CIDR ranges**, forwarding to `osmt-staff` on 8080.

Route53 records point `osmt.example.edu` at the public ALB and
`osmt-admin.example.edu` at the internal one. The internal record usually
belongs in a private hosted zone. One ACM certificate covering both names —
a wildcard for the parent domain is simplest.

OSMT reads standard forwarded headers, so TLS termination at the ALB works
without extra configuration:

```properties
server.tomcat.remoteip.remote-ip-header=x-forwarded-for
server.tomcat.remoteip.protocol-header=x-forwarded-proto
```

Health checks target `/health` on port 8080. See
[Deployment and rollout](#deployment-and-rollout) for the timing these probes
need.

### Security groups

| Source | Destination | Port |
|---|---|---|
| Internet | Public ALB | 443 |
| VPN CIDRs | Internal ALB | 443 |
| Both ALB SGs | EKS node SG | 8080 |
| EKS node SG | Aurora SG | 3306 |
| EKS node SG | Redis SG | 6379 |
| EKS node SG | Elasticsearch (in-cluster) | 9200 |

### Configuration and secrets

Put non-secret configuration in **SSM Parameter Store** and credentials in
**Secrets Manager**, both encrypted with a customer-managed **KMS** key. Inject
them into pods through your preferred mechanism — the External Secrets Operator
and the Secrets Store CSI driver are both common. OSMT does not care where
values come from; it reads environment variables.

### Observability

CloudWatch Container Insights for cluster and pod metrics, plus the cluster log
groups. If you run an APM agent such as Dynatrace OneAgent, it deploys as a
DaemonSet and needs no OSMT-specific configuration.

## Sizing

This is a low-traffic application. A skills catalog serves a modest, bursty
read load and a handful of concurrent authors. Size for reliability and
recovery, not throughput.

| Component | Recommendation | Scales with |
|---|---|---|
| EKS nodes | 2–3 Graviton instances, `m7g.large` class, spread across AZs | pod count |
| `osmt-public` pods | 2 replicas, ~1 vCPU / 2–3 GiB requested | public read traffic |
| `osmt-staff` pods | 1–2 replicas, ~1 vCPU / 2–3 GiB requested | concurrent authors |
| Aurora MySQL | Serverless v2, ~0.5–2 ACU | catalog size, author activity |
| ElastiCache Redis | single small node, `cache.t4g.micro` class | staff session count |
| Elasticsearch | 1–2 nodes, gp3 volumes | catalog size |
| NAT gateway | one, shared | — |

Notes on the less obvious choices:

**Aurora Serverless v2 over provisioned instances.** At this traffic level the
cluster is idle most of the time. Serverless v2 with a low ACU floor generally
costs less than a `db.t4g.medium` writer plus a reader, and it absorbs import
and reindex bursts without a resize. If your institution standardizes on
provisioned Aurora, a `db.t4g.medium` writer with one reader is the equivalent.

**A single Elasticsearch node is defensible.** The search index is **derived
state** — it is rebuilt from MySQL by the staff instance on demand. Losing it
costs a reindex, not data. Size for query latency and index size, and do not
pay for heavy replication you do not need. Two nodes if you want to survive a
node failure without a rebuild.

**Match the image architecture to the node architecture.** If you build OSMT
images for ARM64, run Graviton nodes; if x86, run x86 nodes. A mismatch
produces a pod that fails to start with an exec-format error, which is an
irritating thing to diagnose at 2am. Pick one and be consistent.

**JVM heap below the container limit.** OSMT is a Spring Boot application. Set
the heap explicitly and leave headroom for non-heap memory, or the kernel will
OOM-kill pods that look like they have room.

## Configuring the two instances

Both Deployments run the same image. Configuration is what makes them
different.

### Required variables

The container entrypoint validates these and exits if any is empty:

| Variable | Notes |
|---|---|
| `BASE_DOMAIN` | The hostname this instance serves — different per instance |
| `ENVIRONMENT` | Comma-separated Spring profile list |
| `DB_URI` | `user:password@host:port` — see [Database](#database-aurora-mysql) |
| `REDIS_URI` | **Bare `host:port`, no scheme** — see below |
| `ELASTICSEARCH_URI` | Full URL, e.g. `https://osmt-es-http.osmt.svc:9200` |

### Side-by-side configuration

| Setting | `osmt-public` | `osmt-staff` |
|---|---|---|
| Profiles in `ENVIRONMENT` | base + `readonly` | base; `oauth2` appended by entrypoint |
| `BASE_DOMAIN` | `osmt.example.edu` | `osmt-admin.example.edu` |
| `FRONTEND_URL` | `https://osmt.example.edu` | `https://osmt-admin.example.edu` |
| Database user | read-only user | read-write user |
| Database endpoint | Aurora **reader** | Aurora **writer** |
| `MIGRATIONS_ENABLED` | `false` — set it explicitly | `true` |
| `REINDEX_ELASTICSEARCH` | `false` | `true` |
| `SKIP_METADATA_IMPORT` | `true` after first load | `true` after first load |
| OAuth variables | **all must be absent**, Google included | set — see [Authentication](#authentication-on-the-staff-domain) |
| `OSMT_PUBLIC_INSTANCE_URL` | not set | `https://osmt.example.edu` |
| `OSMT_BRAND_COLOR` | institution brand | deliberately different |
| `APP_SESSION_TOKEN_SECRET` | not needed | required |

### Profile selection

`ENVIRONMENT` carries a comma-separated Spring profile list, for example
`staging,apiserver`.

One of those entries must be an **environment profile the entrypoint
recognizes**, and the recognized set is exactly `dev`, `test`, `review`, and
`staging`. There is no `production` profile. If `ENVIRONMENT` contains none of
the four, the startup metadata import and reindex steps are invoked with a
malformed profile string and run without environment configuration. Pick
`staging` for a production deployment, or add your own profile properties file
and extend the entrypoint's list.

The entrypoint then adds to that list:

- If a complete OAuth variable set is present, it appends `oauth2`.
- If no OAuth variables are present, it appends `single-auth`.
- If OAuth is configured and `ENABLE_SINGLE_AUTH=true`, it appends `single-auth`
  alongside `oauth2`, so the login page offers both.

Add `readonly` to the public instance's `ENVIRONMENT` yourself.

Leave `ENABLE_SINGLE_AUTH` **unset in production**. It exists so staging and
demo environments can offer a local admin form beside the identity provider. A
VPN-gated authoring site using institutional SSO does not need a second way in.

### Redis

Both instances need a reachable Redis. The public instance stores no sessions —
`readonly` sets `spring.session.store-type=none` — but `REDIS_URI` is still
required at startup, the Redis health indicator is active, and Redis backs
OSMT's async task messaging for imports and exports. One small shared node
serves both.

`REDIS_URI` must be a **bare `host:port`** with no URI scheme, because the
scheme is applied by the configuration template:

```properties
redis.uri=${REDIS_URI:localhost:6379}
spring.redis.url=redis://${redis.uri}
```

Passing `rediss://my-cache:6379` produces `redis://rediss://my-cache:6379` and
fails. Because the scheme is fixed at `redis://`, ElastiCache **in-transit
encryption is not usable** with this configuration — rely on VPC placement and
security-group isolation, which is why Redis sits in a private subnet reachable
only from the node security group.

### The `FRONTEND_URL` trap

If `FRONTEND_URL` is not set, the entrypoint defaults it to
`http://${BASE_DOMAIN}` — plain HTTP — while `application.properties` derives
the base URL as HTTPS:

```properties
app.baseUrl=https://${app.baseDomain}
app.loginSuccessRedirectUrl=${app.frontendUrl}/login/success
```

Behind a TLS-terminating ALB, set `FRONTEND_URL` explicitly to the `https://`
URL on both instances. Otherwise the post-login redirect sends staff to an HTTP
URL and the sign-in flow appears to hang or loop.

### Session tokens

After OAuth sign-in the staff instance issues its own signed JWT rather than
passing the identity provider's token to the browser. Configure:

| Variable | Notes |
|---|---|
| `APP_SESSION_TOKEN_SECRET` | Base64-encoded, at least 256 bits — `openssl rand -base64 32` |
| `APP_SESSION_TOKEN_EXPIRY_SECONDS` | Default 86400 (24h) |
| `APP_SESSION_TOKEN_ISSUER` | Defaults to the app base URL |

Store the secret in Secrets Manager and expose it only to the staff
ServiceAccount. Rotating it invalidates all active sessions.

## Authentication on the staff domain

The staff instance authenticates against your institutional identity provider
over OIDC. The worked example below uses PingFederate; the Entra ID variations
follow.

OSMT's generic OIDC client registration is currently named `okta`. Any
OIDC-compliant provider goes in that slot, but the callback path and the login
button label are fixed to that name — worth knowing before you configure the
redirect URI.

### PingFederate

**1. Create an OIDC client.** Authorization code flow, confidential client
(client authentication with a shared secret).

**2. Set the redirect URI:**

```
https://osmt-admin.example.edu/login/oauth2/code/okta
```

**3. Grant the scopes** `openid`, `profile`, `email`.

**4. Configure a claim carrying group or role membership.** OSMT reads the
claim named by `app.oauth2.rolesClaim`, which defaults to `roles`. Map your
staff groups onto the role values OSMT expects:

```properties
osmt.security.role.admin=ROLE_Osmt_Admin
osmt.security.role.curator=ROLE_Osmt_Curator
osmt.security.role.view=ROLE_Osmt_View
```

If you do not want role mapping at all, set `app.enableRoles=false` and any
authenticated user gets access. For an authoring site that is usually too
coarse — curators and admins have meaningfully different reach — but it is a
reasonable first step during a pilot.

**5. Set the environment variables** on the staff Deployment:

| Variable | Value |
|---|---|
| `OAUTH_ISSUER` | Your PingFederate OIDC issuer URL |
| `OAUTH_CLIENTID` | Client ID |
| `OAUTH_CLIENTSECRET` | Client secret — from Secrets Manager |
| `OAUTH_AUDIENCE` | Expected audience value |

All four must be present. The entrypoint only appends the `oauth2` profile when
it sees the complete set; a partial configuration silently falls back to
single-auth, which is not what you want on a production authoring site.

If your provider issues bearer tokens for API access under a different issuer
than the login flow uses, set `OAUTH_JWT_ISSUER` to the token issuer. Otherwise
it follows `OAUTH_ISSUER`.

### Microsoft Entra ID

The shape is identical; four things differ. The issuer is
`https://login.microsoftonline.com/{tenant-id}/v2.0`, and the app registration
lives in Entra with the same redirect URI
(`https://osmt-admin.example.edu/login/oauth2/code/okta`). Group membership
arrives one of two ways: the `groups` claim carries directory object IDs, which
means mapping opaque GUIDs onto OSMT roles, or you define **app roles** on the
registration and they arrive in the `roles` claim — the latter maps cleanly
onto `app.oauth2.rolesClaim` and is the option to prefer. Client secrets in
Entra expire, with a maximum lifetime measured in months, so plan rotation
before you deploy rather than discovering it when authoring stops working.
Finally, if your tenant enforces conditional access on the application, confirm
the policy accommodates a browser flow originating from your VPN ranges.

### OIDC and the VPN-restricted load balancer

The staff hostname resolves and routes only over the VPN, and the redirect URI
you register points at that hostname. This is fine, and it is worth being clear
about why: OIDC authorization code redirects are **browser-mediated**. The
identity provider returns a redirect to the user's browser, and the browser —
which is on the VPN — makes the request. Your identity provider never needs to
reach `osmt-admin.example.edu` itself.

The parts that do need care are DNS and certificates. The browser must resolve
the internal hostname (private hosted zone, reachable over the VPN) and must
trust the certificate the internal ALB presents.

## White-label theming

Every visible branding element can be overridden per instance. Set these on the
**API container only** — the Angular UI fetches `/whitelabel/whitelabel.json`
from the API at runtime, so a branding change needs a pod restart, not an image
rebuild.

| Variable | Purpose |
|---|---|
| `OSMT_TOOL_NAME` | Short name — browser tab, header |
| `OSMT_TOOL_NAME_LONG` | Full name shown as a tagline |
| `OSMT_BRAND_COLOR` | Primary brand color, hex |
| `OSMT_LOGO_URL` | Logo image location |
| `OSMT_LICENSE_PRIMARY` | Footer line 1 |
| `OSMT_LICENSE_SECONDARY` | Footer line 2 |
| `OSMT_WHITELABEL_JSON` | Full JSON object override |

Values merge in layers, later winning: packaged defaults, then
`OSMT_WHITELABEL_JSON`, then individual `OSMT_*` variables, then auth-related
fields supplied by Spring. Set only what you want to change.

### Differentiating the two instances

Give the public instance your institutional brand color and the staff instance
a visibly different one. This is not decoration — it is the cheapest possible
guard against an author believing they are editing when they are browsing, or
pasting a staff URL into a public communication.

```yaml
# osmt-public
- name: OSMT_TOOL_NAME
  value: "Example University Skills"
- name: OSMT_BRAND_COLOR
  value: "#00558c"

# osmt-staff
- name: OSMT_TOOL_NAME
  value: "Skills Authoring"
- name: OSMT_BRAND_COLOR
  value: "#7d0e0e"
```

The staff instance can also set `OSMT_AUTHORING_WELCOME_MESSAGE` to put a line
of copy on its login page, and should set `OSMT_PUBLIC_INSTANCE_URL` to the
public site's base URL so published links point at the right place.

### Providing a logo

`OSMT_LOGO_URL` accepts an external URL, a served path, or a data URI. On
Kubernetes, the cleanest option is a **ConfigMap** mounted into the pod. It
keeps branding in-cluster, versioned with the rest of your manifests, and free
of any external fetch:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: osmt-branding
data:
  logo.svg: |
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 220 56">...</svg>
---
# in the pod spec
volumes:
  - name: branding
    configMap:
      name: osmt-branding
containers:
  - name: osmt
    volumeMounts:
      - name: branding
        mountPath: /opt/osmt/whitelabel
    env:
      - name: WHITELABEL_PATH
        value: /opt/osmt/whitelabel
      - name: OSMT_LOGO_URL
        value: /whitelabel/logo.svg
```

Files in `WHITELABEL_PATH` are served under `/whitelabel/`. ConfigMaps cap at
roughly 1 MiB, which is ample for an SVG.

The resource handler is registered only if `WHITELABEL_PATH` resolves to an
existing directory. A typo in the mount path is not an error — the handler is
simply never registered, and the logo 404s while everything else works. If the
logo does not appear, check the mount before you check the variable.

If your institution already centralizes brand assets on a CDN, pointing
`OSMT_LOGO_URL` at that URL works equally well and may fit your governance
better. A data URI is possible for very small marks but runs into
platform-specific environment variable size limits.

### Contrast

The navigation bar uses your brand color as its background, so the logo must
contrast against it — a dark mark on a dark brand color disappears. The UI
measures the contrast ratio between white text and your brand color and
switches to dark text when it falls below the WCAG AA threshold of 4.5:1, but
it cannot recolor your logo for you. The logo renders at roughly 110 × 28 px;
SVG is recommended.

## Database: Aurora MySQL

OSMT runs on **Aurora MySQL 8.0**.

### The `sql_mode` requirement

This is the most important database detail in this document. OSMT's schema
depends on a specific `sql_mode`, and Aurora's default breaks it. Set this on
**both** the cluster parameter group and the DB instance parameter group:

```
ONLY_FULL_GROUP_BY,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION
```

Notably absent are `STRICT_TRANS_TABLES` and `NO_ZERO_DATE`. OSMT's schema
carries zero-date and nullability conventions that strict mode rejects. If you
skip this, symptoms appear at migration time or during writes and look like
application defects rather than configuration.

### Two database users

**The read-write user** is used by the staff instance and by any migration or
import job. Use Aurora's managed master password so the credential is generated
into Secrets Manager and encrypted with your KMS key.

**The read-only user** is used by the public instance. Your infrastructure
tooling will not create this for you — it is an explicit step against the
running cluster:

```sql
CREATE USER IF NOT EXISTS 'osmt_ro'@'%' IDENTIFIED BY '<password>';
GRANT SELECT ON osmt_db.* TO 'osmt_ro'@'%';
FLUSH PRIVILEGES;
```

Store that password in Secrets Manager and expose it only to the public
instance's ServiceAccount.

### Endpoints

Point the **staff** instance at the Aurora **writer** endpoint and the
**public** instance at the **reader** endpoint. Combined with the `SELECT`-only
grant, that gives two independent layers of read-only enforcement — a
misconfigured grant is still caught by the reader endpoint, and vice versa.

### Connection configuration

The entrypoint requires `DB_URI` in the form `user:password@host:port`, and the
database name is supplied separately:

```properties
db.uri=${DB_URI:${db.user}:${db.password}@${db.host}:${db.port}}
db.composedUrl=jdbc:mysql://${db.uri}/${db.name}
```

Compose `DB_URI` at pod startup from the credential your secret store provides,
and set `DB_NAME` to your database (default `osmt_db`).

### Migrations

The **staff instance owns the schema**. It runs Flyway on startup when
`MIGRATIONS_ENABLED=true`.

Set `MIGRATIONS_ENABLED=false` on the public instance explicitly. Although
`application-readonly.properties` contains `spring.flyway.enabled=false`, the
entrypoint passes `-Dspring.flyway.enabled=${MIGRATIONS_ENABLED}` as a JVM
system property, which outranks the profile's packaged value. The profile alone
does not authoritatively disable migrations — the variable does. The default is
`false`, so an unset variable is safe; an inherited `true` from a shared
configuration template is not, and would attempt migrations through the reader
endpoint using a `SELECT`-only grant.

The consequence for rollouts: when a release carries a migration, the staff
instance must reach the new version first. Deploy staff, let migrations
complete, then deploy public.

Backups, maintenance windows, and deletion protection follow your normal Aurora
practice; nothing about OSMT changes those choices.

## Restoring from a database snapshot

Institutions commonly seed a new environment from an existing snapshot. The
order of operations matters more than usual here.

**1. Restore the snapshot.** This creates a **new cluster** with new endpoints.
Repoint every reference — SSM parameters, Secrets Manager entries, pod
configuration.

**2. Reattach the parameter groups.** Parameter groups do **not** travel with a
snapshot. The restored cluster comes up on defaults, which means the `sql_mode`
above is gone. Reattach both the cluster and instance parameter groups before
starting OSMT against it.

**3. Fix the user credentials.** Database user accounts *do* travel inside the
snapshot, because they live in the `mysql` schema. So the restored `osmt_ro`
user exists with the password it had when the snapshot was taken — which will
not match a freshly generated secret in your new environment. Either update the
secret to the old value, or reset the account:

```sql
ALTER USER 'osmt_ro'@'%' IDENTIFIED BY '<new-password>';
FLUSH PRIVILEGES;
```

Reset the master password the same way and re-enable managed rotation.

**4. Start the staff instance first.** It runs Flyway and migrates the restored
schema forward to the current application version. Starting the public instance
first points read-only pods at an unmigrated schema — which does not crash, it
serves wrong or incomplete results. This is the failure mode most likely to go
unnoticed.

**5. Reindex Elasticsearch.** The search index is derived from MySQL, so a
restore leaves it describing the previous contents. The staff instance rebuilds
it on startup when `REINDEX_ELASTICSEARCH=true`. Until that finishes, search
returns stale results even though the database is correct.

**6. Verify.** Confirm row counts against expectation; confirm the read-only
user can `SELECT` and cannot `INSERT`; confirm search returns restored content;
confirm the public site serves it.

## Search: Elasticsearch on ECK

Run **Elasticsearch 8.x**, deployed with the **ECK (Elastic Cloud on
Kubernetes) operator** using the official Elasticsearch images, in the same EKS
cluster.

Use Elasticsearch rather than Amazon OpenSearch Service. The Elasticsearch 8
client library that OSMT's data layer depends on sends a compatibility header
that OpenSearch rejects, so OpenSearch is not a drop-in substitute today.

### Shape

Install the ECK operator, then declare an `Elasticsearch` resource with one or
two nodes and gp3-backed persistent volumes. Pods reach it over the in-cluster
service DNS name; set `ELASTICSEARCH_URI` accordingly, for example
`https://osmt-es-http.osmt.svc:9200`.

ECK enables TLS and generates an elastic user credential by default. Supply
those to OSMT through `ELASTICSEARCH_USERNAME` and `ELASTICSEARCH_PWD`, sourced
from the operator-managed secret. If you disable ECK's security features
instead, both variables can be omitted.

### Index ownership

The **staff instance owns the index lifecycle**. It rebuilds the index at
startup when `REINDEX_ELASTICSEARCH=true`. The public instance reads the same
index and must have `REINDEX_ELASTICSEARCH=false` — two instances reindexing
concurrently is not a supported arrangement.

Because the index is rebuildable from MySQL, snapshotting Elasticsearch itself
is optional. Treat a lost index as a restart cost, not a data loss event.

## Deployment and rollout

### Startup is slow

OSMT does real work before it can serve traffic: schema migration, optional
metadata import, and — on the staff instance — a full Elasticsearch reindex.
Probes must accommodate this or Kubernetes will kill pods mid-initialization
and never converge.

Use a `startupProbe` against `/health` with a generous failure budget, and keep
`livenessProbe` conservative. For reference, the equivalent ECS deployment
allows a 300-second container start period and a 600-second load-balancer grace
period. Budget similarly.

Note that `/health` reflects database, Redis, **and** Elasticsearch health.
A pod will not report healthy while any of the three is unreachable, which is
usually what you want, but it means a Redis blip shows up as an application
outage.

### First boot versus steady state

On a first deployment against an empty database, leave `SKIP_METADATA_IMPORT`
unset or `false` so the BLS and O*NET reference data loads. Once loaded, set
`SKIP_METADATA_IMPORT=true` for all subsequent deployments — the import is slow
and re-running it on every pod start is pure cost.

### Rollout order

1. Deploy `osmt-staff`. Wait for migrations and reindex to complete.
2. Deploy `osmt-public`.

A staff rollout that triggers a reindex is not zero-cost. Where the catalog is
large, prefer a maintenance window.

## Operational notes

A single page of things worth knowing before you are debugging them.

- **Any OAuth variable on the public instance breaks it.** The three security
  configurations are mutually exclusive by profile; adding `oauth2` leaves none
  of them active and the pod either crash-loops or demands a login for every
  request. The Google client id and secret count, not just the four
  PingFederate variables.
- **Set `MIGRATIONS_ENABLED=false` on the public instance explicitly.** The
  entrypoint's JVM system property outranks the `readonly` profile's value.
- **`FRONTEND_URL` must be set explicitly to the HTTPS URL.** Its default is
  `http://${BASE_DOMAIN}`, which breaks the post-login redirect behind an ALB.
- **`REDIS_URI` takes a bare `host:port`.** Any scheme prefix produces a
  malformed URL. In-transit encryption is therefore unavailable; isolate Redis
  at the network layer.
- **`sql_mode` must be pinned** on both the cluster and instance parameter
  groups, and it does not survive a snapshot restore.
- **The read-only database user is a manual step.** Nothing creates it for you.
- **Migrations belong to the staff instance.** Deploy it first on any release
  carrying a schema change.
- **The search index is derived state.** After any database restore, reindex
  before trusting search results.
- **Match image architecture to node architecture.** ARM64 images need Graviton
  nodes.

### Where configuration lives

| Kind | Store | Examples |
|---|---|---|
| Secrets | Secrets Manager | DB credentials, OAuth client secret, session token secret |
| Configuration | SSM Parameter Store | hostnames, public instance URL, feature flags |
| Branding assets | ConfigMap | logo SVG |
| Profiles | Deployment env | `ENVIRONMENT`, `REINDEX_ELASTICSEARCH` |
