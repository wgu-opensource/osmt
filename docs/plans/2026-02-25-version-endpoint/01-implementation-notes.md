# Implementation Notes

## Files changed (OSMT repo) ✅

- `api/pom.xml` – Resource filtering for `version.json`; git-commit-id properties used
- `api/src/main/resources/version.json` – Template with `${git.commit.id.abbrev}`, `${git.build.time}`
- `api/src/main/kotlin/edu/wgu/osmt/VersionController.kt` – New controller serving `/version`
- `api/src/main/resources/config/application.properties` – Disable actuator info endpoint (replaced by `/version`)

## Schema

| Field | Type | Description |
|-------|------|-------------|
| version | string | OSMT version: git tag if on a tag, otherwise abbrev commit hash |
| buildTimestamp | string | ISO8601 timestamp of the build |
| extra | object | Optional. Populated by build context (e.g. monorepo adds `sbsMonorepoVersion`) |

OSMT does not interpret `extra`; it relays it verbatim for whatever system built the image.
