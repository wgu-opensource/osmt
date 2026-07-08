# Phase 7: Cleanup and Validation

## Scope of Phase

- Remove temporary code, TODOs, debug prints
- Fix warnings and formatting
- Run full test suite
- Document changes in summary

## Code Organization Reminders

- No temporary code in final commit
- All TODOs either resolved or tracked as follow-up

## Implementation Details

### 1. Grep for Temporary Code

```bash
git diff | grep -E "TODO|FIXME|console\.log|println|debug"
```

Remove or resolve any found.

### 2. Format and Lint

```bash
cd ui && npm run format:check && npx prettier --write "src/**/*.ts"
cd api && mvn ktlintCheck  # or project's lint
```

### 3. Full Validation

```bash
sdk env
mvn clean test
cd ui && npm run test
cd ui && npm run format:check
```

### 4. Plan Summary

Add `docs/plans/2026-03-04-sync-pagination-retry-status/summary.md`:

- Summary of changes
- New files, modified files
- Config properties added
- Migration added

### 5. Move Plan to docs/plans-done/

After validation passes, move the plan directory to `docs/plans-done/2026-03-04-sync-pagination-retry-status/`.

### 6. Commit

```bash
git add -A
git commit -m "feat(sync): pagination, retry with backoff, status_json for debugging

- Add status_json to SyncState for job-level status and error tracking
- Batch processing (default 20) with incremental watermark updates
- Retry 5x with exponential backoff (5s × 1.5^attempt, max 60s)
- Stop on first record failure; record error and correlation ID in status_json
- UI displays status, error, and copyable correlation ID
- SyncRetryHelper with fail-safe caps (retry ≤10, delay ≤60s)"
```

## Validate

```bash
mvn clean test
cd ui && npm run test && npm run format:check
```
