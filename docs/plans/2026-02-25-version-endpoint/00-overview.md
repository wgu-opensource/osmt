# Version Endpoint for Deployment Verification

## Summary

Add a `GET /version` endpoint to OSMT that returns build and version information. This enables deployment verification—confirming which code version is running in staging or production without inspecting logs or image metadata.

## Scope

- **`/version`** – Returns a JSON object with `version` (OSMT git tag or commit), `buildTimestamp` (ISO8601), and optional `extra` (build-context-specific data)
- **Standalone builds** – When OSMT is built from its own repo (`mvn package`), version comes from git-commit-id-plugin
- **Monorepo builds** – When built from skybridgeskills-monorepo, the Docker build injects monorepo version info into the `extra` field

## Client-facing outcome

Deployments can be verified by requesting `/version`:

```bash
curl https://staging.osmt.example.com/version
```

Response example:
```json
{
  "version": "7c5fe52",
  "buildTimestamp": "2026-02-25T15:30:00Z",
  "extra": {
    "sbsMonorepoVersion": "2026.02.25-1"
  }
}
```

When built standalone (outside monorepo), `extra` is empty or omitted.

## Technical approach

1. **version.json** – Generated at build time, embedded in the JAR as a classpath resource
2. **VersionController** – Serves the file verbatim at `/version`
3. **Build contexts** – Maven generates for standalone; monorepo Dockerfile overwrites with additional data when applicable

## References

- Monorepo plan: `docs/plans/2026-02-25-osmt-version-endpoint/` (in skybridgeskills-monorepo)
