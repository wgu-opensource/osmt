# Phase 5: Cleanup, Review, and Validation

## Scope of Phase

Final cleanup and validation:

1. Remove any TODO comments that were meant to be temporary
2. Check for debug prints or logging statements
3. Run full test suite
4. Verify formatting
5. Create summary

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Cleanup Checklist

### 1. Search for TODO Comments

```bash
cd /Users/yona/dev/skybridge/osmt
rg "TODO" api/src/main/kotlin/edu/wgu/osmt/security/ api/src/main/kotlin/edu/wgu/osmt/config/ api/src/main/kotlin/edu/wgu/osmt/ui/
rg "TODO" ui/src/app/auth/ ui/src/app/navigation/ ui/src/app/models/
```

Remove or resolve any TODOs that were meant to be temporary.

### 2. Search for Debug Logging

```bash
rg "println|console.log|logger.debug" api/src/main/kotlin/edu/wgu/osmt/security/ReadOnlySecurityConfig.kt
```

### 3. Check Test Coverage

Ensure all new code has corresponding tests:

- `ReadOnlySecurityConfig.kt` → `ReadOnlySecurityConfigTest.kt`
- `AppConfig` new fields → Verified in existing tests
- `WhitelabelConfig` new env vars → Verified in `WhitelabelControllerTest`
- `UiController` new fields → `WhitelabelControllerTest`
- Frontend models/components → Unit tests in `.spec.ts` files

### 4. Verify No Unused Imports

```bash
cd /Users/yona/dev/skybridge/osmt/api
mvn compile -q
# Check for warnings about unused imports
```

```bash
cd /Users/yona/dev/skybridge/osmt/ui
npm run lint
# Check for unused imports or variables
```

## Validate

### Backend Tests

```bash
cd /Users/yona/dev/skybridge/osmt/api
sdk env install
mvn test -q
```

Expected: All tests pass, including new `ReadOnlySecurityConfigTest`.

### Frontend Tests

```bash
cd /Users/yona/dev/skybridge/osmt/ui
npm test -- --watch=false --browsers=ChromeHeadless
```

Expected: All tests pass, including updated login and header tests.

### Frontend Formatting

```bash
cd /Users/yona/dev/skybridge/osmt/ui
npm run format:check
```

If there are issues:

```bash
npx prettier --write "src/app/**/*.{ts,html}"
```

### Backend Formatting

For Kotlin, ensure consistent formatting:

```bash
cd /Users/yona/dev/skybridge/osmt/api
# Check for consistent indentation and style
# Follow existing patterns in the codebase
```

### Integration Test

Manually test the split deployment configuration:

1. **Start read-only instance:**
   ```bash
   cd /Users/yona/dev/skybridge/osmt
   SPRING_PROFILES_ACTIVE=readonly OSMT_INSTANCE_TYPE=read-only OSMT_WRITABLE_INSTANCE_URL=https://author.example.com ./osmt_cli.sh -s
   ```

2. **Verify whitelabel JSON:**
   ```bash
   curl http://localhost:8080/whitelabel/whitelabel.json | jq
   ```
   Expected: Contains `instanceType: "read-only"` and `writableInstanceUrl`.

3. **Verify login page:**
   - Navigate to `http://localhost:8080/login`
   - Should show read-only message with link to writable instance
   - No OAuth buttons or login form

4. **Verify header:**
   - No login button in the menu
   - Link to writable instance (if configured)

## Plan Cleanup

Add a summary of the completed work to `docs/plans/2026-04-13-split-deployment/summary.md`:

```markdown
# Split Deployment Implementation Summary

## Completed Work

### Phase 1: Backend Read-Only Profile
- Created `application-readonly.properties` with read-only defaults
- Created `ReadOnlySecurityConfig.kt` for no-auth security
- Added `readOnlyMode` and `instanceType` to `AppConfig`

### Phase 2: Whitelabel Extensions
- Added new environment variables to `WhitelabelConfig`
- Updated `UiController` to include split deployment fields
- Updated `api/docker/whitelabel/whitelabel.json` with examples

### Phase 3: Frontend UI Adaptations
- Updated `IAppConfig` model with split deployment fields
- Updated `login.component.ts` and `.html` for read-only display
- Updated `header.component.ts` and `.html` to hide login in read-only mode
- Added unit tests for new behavior

### Phase 4: Documentation
- Created `docs/features/2026-04-13-split-deployment.md`
- Updated whitelabel documentation with split deployment theming

## Files Changed

- `api/src/main/resources/config/application-readonly.properties` (NEW)
- `api/src/main/kotlin/edu/wgu/osmt/security/ReadOnlySecurityConfig.kt` (NEW)
- `api/src/main/kotlin/edu/wgu/osmt/security/ReadOnlySecurityConfigTest.kt` (NEW)
- `api/src/main/kotlin/edu/wgu/osmt/config/AppConfig.kt` (UPDATE)
- `api/src/main/kotlin/edu/wgu/osmt/config/WhitelabelConfig.kt` (UPDATE)
- `api/src/main/kotlin/edu/wgu/osmt/ui/UiController.kt` (UPDATE)
- `api/docker/whitelabel/whitelabel.json` (UPDATE)
- `ui/src/app/models/app-config.model.ts` (UPDATE)
- `ui/src/app/auth/login.component.ts` (UPDATE)
- `ui/src/app/auth/login.component.html` (UPDATE)
- `ui/src/app/navigation/header.component.ts` (UPDATE)
- `ui/src/app/navigation/header.component.html` (UPDATE)
- `docs/features/2026-04-13-split-deployment.md` (NEW)
- `docs/features/2026-03-19-whitelabel-theming.md` (UPDATE)

## Testing

All tests pass:
- Backend unit tests
- Frontend unit tests
- Manual integration test of read-only mode

## Usage

To deploy OSMT in split mode:

1. Configure read-only instance with `SPRING_PROFILES_ACTIVE=readonly`
2. Set `OSMT_INSTANCE_TYPE=read-only` and `OSMT_WRITABLE_INSTANCE_URL`
3. Configure writable instance with normal auth profile
4. Set different `OSMT_BRAND_COLOR` and `OSMT_LOGO_URL` for visual distinction
```

## Move Plan to Done

After validation and commit:

```bash
mkdir -p docs/plans-done
cp -r docs/plans/2026-04-13-split-deployment docs/plans-done/
```

## Commit

Create a commit following [Conventional Commits](https://www.conventionalcommits.org/) format:

```
feat: split deployment support for read-only and writable instances

- Add readonly Spring profile with no-auth security config
- Add split deployment fields to whitelabel JSON
- Add instanceType, writableInstanceUrl, readOnlyMessage to AppConfig
- Update login page to show read-only message when applicable
- Update header to hide login button in read-only mode
- Add comprehensive split deployment documentation
```
