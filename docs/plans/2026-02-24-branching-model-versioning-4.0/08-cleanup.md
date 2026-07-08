# Phase 8: Cleanup & Validation

## Scope of phase

Final cleanup, validation, and plan summary. Remove temporary code, fix warnings, ensure all validations pass.

## Code Organization Reminders

- No nested code more than 2 levels deep
- Remove TODO comments that were for development only
- Follow .eslintrc.json, .prettierrc, .editorconfig

## Implementation Details

### 1. Grep for temporary artifacts
```bash
cd /Users/yona/dev/skybridge/osmt
rg "TODO|FIXME|XXX|HACK|debug|console\.log" \
  --glob '!node_modules' --glob '!*.lock' \
  -g '!docs/plans*' 2>/dev/null || true
```
- Remove or resolve any unintended TODOs in new/modified files

### 2. Validate versions script
- Ensure `bin/validate-versions.sh` still passes with 0.0.0-SNAPSHOT
- If it validates app version consistency, update to accept 0.0.0-SNAPSHOT as valid placeholder

### 3. Linting and formatting
```bash
cd ui && npm run format:check && npm run lint
cd .. && mvn validate -q
```

### 4. Full build and test
```bash
sdk env 2>/dev/null || true
mvn clean install -DskipTests
cd ui && npm run build-prod && npm run ci-test
cd ..
```

### 5. Plan summary
- Add `summary.md` to plan directory with completed work summary
- Move plan files to `docs/plans-done/` when done (per plan process)

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt

# Full validation
sdk env 2>/dev/null || true
mvn clean install -pl api -am
cd ui && npm run format:check && npm run lint && npm run ci-test && npm run build-prod
cd ..

# Version validation
bin/validate-versions.sh
```

## Plan Cleanup
- Create `docs/plans/2026-02-24-branching-model-versioning-4.0/summary.md`
- After commit: move plan dir to `docs/plans-done/`

## Commit
```
chore(release): migrate to main branch and 4.0 versioning

- Add docs/versioning.md with versioning strategy
- Set version files to 0.0.0-SNAPSHOT placeholder
- Add bin/tag-next-version.sh for post-merge tagging
- Add .github/workflows/main-push.yml
- Update ci.yml and trigger-monorepo-version-bump.yml to use main
- Update CONTRIBUTING.md branching model
- Document branch migration steps
```
