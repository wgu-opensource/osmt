# Phase 7: Config and Integration

## Scope of Phase

- application.properties CE config
- Docker/env documentation
- Wire SyncStateTable into ApiServer tableList
- Integration tests; format; validation

## Code Organization Reminders

- Keep config minimal; document in osmt-staging.env.example
- Run full test suite

## Implementation Details

### 1. Application Properties

Update `api/src/main/resources/config/application.properties`:

```properties
# Credential Engine (optional - when absent, mock in dev / disabled in prod)
credential-engine.api-key=${CREDENTIAL_ENGINE_API_KEY:}
credential-engine.org-ctid=${CREDENTIAL_ENGINE_ORG_CTID:}
credential-engine.registry-url=${CREDENTIAL_ENGINE_REGISTRY_URL:https://sandbox.credentialengine.org}
```

Update `application-dev.properties` if needed (e.g., log level for credentialengine package).

### 2. SyncTargetFactory Config

Ensure SyncTargetFactory reads these props. When all three empty: use Mock in dev profile, null/disabled in prod.

### 3. Docker / Env Documentation

Update `api/osmt-staging.env.example`:

```
# Credential Engine sync (optional)
CREDENTIAL_ENGINE_API_KEY=
CREDENTIAL_ENGINE_ORG_CTID=
CREDENTIAL_ENGINE_REGISTRY_URL=https://sandbox.credentialengine.org
```

Add to docker_entrypoint.sh if env vars need to be passed as JVM system props (likely not – Spring will read from env automatically with ${VAR} syntax).

### 4. ApiServer TableList

Update `api/src/main/kotlin/edu/wgu/osmt/ApiServer.kt`:

Add `SyncStateTable` to `tableList` so schema consistency checks include it.

### 5. Integration Tests

Add or extend API tests:
- `test/api-test/api/v3/sync/state/get.js` – GET sync state (with auth)
- `test/api-test/api/v3/sync/all/post.js` – POST sync all (202)
- Pre-request: login as admin

### 6. Format and Lint

```bash
cd ui && npm run format:check && npx prettier --write .
cd api && mvn formatter:format  # if applicable
```

## Validate

```bash
sdk env
cd api && mvn test
cd ui && npm run test
cd ui && npm run build
cd ui && npm run format:check
```
