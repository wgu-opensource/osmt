# Generic OIDC provider (`oidc` registration)

- Status: accepted
- Date: 2026-07-21

## Context

OSMT authenticates staff against an OIDC identity provider. Historically the
only generic slot for a non-Google provider was a Spring Security client
registration named `okta`. Its issuer is supplied by `OAUTH_ISSUER`, so it
already worked with any OIDC IdP — but the name leaked into three user-visible
places:

- the callback path `/login/oauth2/code/okta`,
- the login button label ("Okta", from a hardcoded map), and
- the login button **icon**, which the Angular UI rendered from the
  `simple-icons` package keyed on the registration id — so a PingFederate
  deployment displayed Okta's trademarked mark.

Institutions want their own label and logo. One near-term client uses
PingFederate and will later move to Microsoft Entra. This repository is a fork
of `wgu-opensource/osmt`, whose upstream runs Okta, so changes should be
additive and mergeable rather than renames of shared files.

Separately, `OAUTH_AUDIENCE` was required to activate the `oauth2` profile but
was never read by the application (the JWT resource server validates issuer, not
audience), making it a misleading, friction-adding knob.

## Decision

Add a fixed generic client registration with id `oidc`, bound to a **new**
`OAUTH_OIDC_ISSUER` / `OAUTH_OIDC_CLIENTID` / `OAUTH_OIDC_CLIENTSECRET` variable
set. Its callback path is `/login/oauth2/code/oidc`. Its button label
(`OAUTH_PROVIDER_NAME`, default "Single sign-on") and icon
(`OAUTH_PROVIDER_ICON_URL` or a curated `OAUTH_PROVIDER_ICON_SLUG`) are
configurable; the generic provider shows **no icon** unless one is configured.
The name and icon flow from the API through `/whitelabel/whitelabel.json` to the
login page.

Drop `OAUTH_AUDIENCE` from the OAuth activation gate (in both
`docker_entrypoint.sh` and `bin/lib/common.sh::detect_security_profile`); the
`oauth2` profile now activates on issuer + client id + secret.

The existing `okta` and `google` registrations are unchanged.

## Alternatives considered

- **Reuse `OAUTH_*` for the generic slot.** Rejected: both `okta` and `oidc`
  would then activate from the same variables, producing two buttons for one
  IdP.
- **Repoint `OAUTH_*` at `oidc` and retire `okta`.** Rejected: it would change
  the callback path for existing Okta deployments from
  `/login/oauth2/code/okta` to `/login/oauth2/code/oidc`, breaking a redirect
  URI they have already registered, and would diverge from upstream.
- **Programmatic `ClientRegistrationRepository`** supporting arbitrary
  runtime-named or multiple simultaneous generic providers. Rejected as
  unnecessary: there is no requirement for two generic providers at once, and it
  replaces Spring Boot autoconfiguration with more code and more upstream-merge
  surface.

## Consequences

- Any OIDC IdP gets a vendor-neutral label, an optional own-branded icon, and a
  stable `/login/oauth2/code/oidc` callback with no code change.
- A PingFederate → Entra migration is an in-place change of issuer and
  credentials; the redirect URI does not change.
- The change is additive; `okta`/`google` behavior is byte-for-byte identical,
  keeping the fork mergeable with upstream.
- Token audience remains unvalidated. Adding an optional audience
  `OAuth2TokenValidator` is recorded as follow-up work, distinct from removing
  the dead gate variable.
