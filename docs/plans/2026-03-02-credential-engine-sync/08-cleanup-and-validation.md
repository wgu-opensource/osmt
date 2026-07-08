# Phase 8: Cleanup and Final Validation

## Scope of Phase

- Remove temporary code, TODOs, debug prints
- Fix warnings, errors, formatting
- Add summary to plan
- Move plan to docs/plans-done/
- Commit with Conventional Commits message

## Cleanup

1. **Grep for temporary code**
   ```bash
   git diff --name-only | xargs grep -l "TODO\|FIXME\|XXX\|console\.log\|println"
   ```
   Remove or resolve each. Keep only intentional TODOs with clear context.

2. **Lint and format**
   ```bash
   cd ui && npm run format:check && npx prettier --write .
   cd api && mvn verify
   ```

3. **Fix all warnings**
   - Resolve compiler warnings
   - Resolve linter warnings in modified files

## Plan Cleanup

1. Add `summary.md` to plan directory with completed work overview
2. Move plan directory to `docs/plans-done/2026-03-02-credential-engine-sync/`

## Validate

```bash
sdk env
cd api && mvn test
cd ui && npm run test
cd ui && npm run build
```

## Commit

```
feat(sync): add Credential Engine sync integration

- SyncState table and repository for watermark tracking
- SyncTarget interface with Mock and CredentialEngine implementations
- SyncService: sync single record, sync since watermark
- SyncController: GET state, POST sync single/all (async)
- UI: /admin/sync management page with Sync Now button
- Config-based mock when CE env vars absent in dev
- Disabled state in prod when CE not configured
```
