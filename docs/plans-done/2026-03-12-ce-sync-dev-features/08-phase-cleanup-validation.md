# Phase 8: Cleanup & Validation

## Scope of Phase

Remove temporary code, TODOs, debug prints. Fix warnings. Update docs. Run full test suite. Add plan summary.

## Code Organization Reminders

- Grep for TODO, FIXME, debug, println.
- Ensure format:check passes.

## Implementation Details

### Grep for temporaries

```bash
git diff | grep -E 'TODO|FIXME|debug|println|console\.log'
```

Remove any found.

### Docs

Update `docs/features/2026-03-03-credential-engine-sync.md`:

- Add `CREDENTIAL_ENGINE_LABEL_PREFIX` and `CREDENTIAL_ENGINE_ALLOW_UNPUBLISH_ALL` to env vars table.
- Add Unpublish All to Actions section.
- Note: delete is for dev cleanup; CE policy prefers deprecation for normal lifecycle.

### Validation

```bash
sdk env && mvn -pl api test -DfailIfNoTests=false
cd ui && npm run format:check && npm run test
```

Fix all failures and format issues.

## Plan Cleanup

Add `summary.md` to plan directory. Move plan to `docs/plans-done/`.

## Commit

```
feat(sync): label prefix and unpublish-all for dev CE testing

- Add CREDENTIAL_ENGINE_LABEL_PREFIX (dev default: (osmt-dev))
- Add credential-engine.allow-unpublish-all (dev default: true)
- Apply prefix to CompetencyLabel and collection Name in CredentialEngineSyncTarget
- Add SyncTarget.unpublishAll(collectionUuids, skillUuids)
- Implement CE delete API in CredentialEngineSyncTarget
- Add POST /api/sync/unpublish-all, extend state with allowUnpublishAll
- Add Unpublish All button to sync management UI when allowed
```
