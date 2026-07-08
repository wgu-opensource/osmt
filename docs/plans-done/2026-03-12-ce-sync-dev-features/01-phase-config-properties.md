# Phase 1: Config Properties

## Scope of Phase

Add `credential-engine.label-prefix` and `credential-engine.allow-unpublish-all` to application.properties. Add dev profile defaults in application-dev.properties.

## Code Organization Reminders

- Place config in the appropriate property files by concern.
- Follow existing naming: `credential-engine.*` for CE-related settings.

## Implementation Details

### application.properties

Add:

```properties
credential-engine.label-prefix=${CREDENTIAL_ENGINE_LABEL_PREFIX:}
credential-engine.allow-unpublish-all=${CREDENTIAL_ENGINE_ALLOW_UNPUBLISH_ALL:false}
```

### application-dev.properties

Create or update `api/src/main/resources/config/application-dev.properties`:

```properties
# Dev defaults for CE sync dev features
credential-engine.label-prefix=${CREDENTIAL_ENGINE_LABEL_PREFIX:(osmt-dev)}
credential-engine.allow-unpublish-all=${CREDENTIAL_ENGINE_ALLOW_UNPUBLISH_ALL:true}
```

The dev profile overrides: when unset, label-prefix defaults to `(osmt-dev)` and allow-unpublish-all to `true`. Env vars still override when explicitly set.

**Note**: Spring profile-specific properties are loaded when `spring.profiles.active` includes `dev`. The `application-dev.properties` file is merged; values there override `application.properties` when the dev profile is active.

## Validate

```bash
sdk env && mvn -pl api compile
```
