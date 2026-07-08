# Summary: Version Endpoint for Deployment Verification

## Completed Work (OSMT repo)

1. **api/pom.xml** – maven-antrun-plugin generates `version.json` at build time from git.properties
   - version: `git.commit.id.abbrev` (fallback: "unknown")
   - buildTimestamp: `git.build.time` with escaped colons normalized
   - extra: `{}` for standalone builds

2. **VersionController.kt** – Serves `GET /version` by returning `version.json` from classpath verbatim

3. **SecurityConfigHelper** – Added `/version` to public endpoints

4. **application.properties** – Disabled actuator info endpoint (was mapped to `/version.json`); replaced by `/version`

## Next Steps

- **Monorepo**: Update `wrappers/osmt/VERSION` to point to OSMT commit that includes these changes
- Run `update-source.sh` then `docker-build.sh` in monorepo to verify Docker build injects `sbsMonorepoVersion` in `extra`
