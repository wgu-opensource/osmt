# Branching Model & Versioning Migration to 4.0

## Scope of Work

This plan covers the migration of OSMT from GitFlow (using `develop` branch) to a simplified workflow using `main` as the primary branch, updating the version to 4.0.0, and implementing an automated post-merge version tagging workflow similar to the monorepo pattern.

### Key Changes:
1. **Branch Migration**: Switch from `develop` to `main` as the primary base branch
2. **Version Update**: Update major version from 3.1.0 to 4.0.0
3. **Automated Versioning**: Implement post-merge workflow that automatically tags patch versions on each merge to `main`
4. **Documentation Updates**: Update all references to `develop` branch in docs, scripts, and workflows
5. **CI/CD Updates**: Update GitHub Actions workflows to use `main` instead of `develop`

## Current State of the Codebase

### Branching Model
- Currently uses GitFlow with `develop` as the integration branch
- `CONTRIBUTING.md` documents GitFlow workflow
- CI workflow (`.github/workflows/ci.yml`) triggers on PRs to `develop` and `feature/*` branches
- Monorepo integration workflow (`.github/workflows/trigger-monorepo-version-bump.yml`) triggers on pushes to `develop`

### Version Information
- **Parent POM** (`pom.xml`): `3.1.0-SNAPSHOT`
- **UI Package** (`ui/package.json`): `1.1.1-SNAPSHOT`
- **API Module** (`api/pom.xml`): Inherits from parent (`3.1.0-SNAPSHOT`)
- Version validation script exists at `bin/validate-versions.sh`

### Files Referencing `develop` Branch
Based on grep search, the following files reference `develop`:
- `.github/workflows/ci.yml` - CI triggers on `develop` branch
- `.github/workflows/trigger-monorepo-version-bump.yml` - Triggers on `develop` pushes
- `CONTRIBUTING.md` - Documents GitFlow with `develop` branch
- Various test files and documentation (likely false positives from word "develop")

### Monorepo Pattern Reference
The monorepo (`skybridgeskills-monorepo`) uses:
- Date-based versioning: `YYYY.MM.DD-buildNumber` format (e.g., `2026.02.24-1`)
- Post-merge workflow on `main` branch that automatically tags versions
- Script: `scripts/tag-next-version.sh` handles version tagging
- Workflow: `.github/workflows/main-push.yml` orchestrates version tagging and builds

## Questions That Need Answers

### Q1: Versioning Strategy ✅ ANSWERED
**Question**: Should OSMT use semantic versioning (4.0.0, 4.0.1, 4.0.2...) or date-based versioning (2026.02.24-1, 2026.02.24-2...) like the monorepo?

**Answer**: Use semantic versioning (4.0.0, 4.0.1, etc.). Patch increments are automatic on each merge to `main`. Minor and major version increments are done manually as needed. This should be documented.

**Notes**: 
- Document that patch increments happen automatically on each merge
- Minor and major versions require manual intervention
- Versions are somewhat arbitrary but follow semantic versioning structure

---

### Q2: Version File Updates ✅ ANSWERED
**Question**: Should the post-merge workflow automatically update version files (`pom.xml`, `package.json`) with the new version, or only create git tags?

**Answer**: Only create tags. No version numbers should be committed to the repo. Scripts should be updated to use git tag version when publishing Docker images.

**Notes**:
- Version files remain unchanged in the repo
- Docker publishing scripts need to extract version from git tags
- This keeps versioning source-of-truth in git tags only

---

### Q3: UI Version Alignment ✅ ANSWERED
**Question**: Should the UI version (`ui/package.json`) align with the parent version (4.0.0) or maintain its own versioning scheme?

**Answer**: Align UI version with parent version (4.0.0) as part of the 4.0 migration.

**Notes**:
- One-time manual update during migration
- UI and API are released together as a single artifact

---

### Q4: SNAPSHOT Handling ✅ ANSWERED
**Question**: Should `main` branch keep `-SNAPSHOT` suffix or use release versions (4.0.0, 4.0.1, etc.)?

**Answer**: Use placeholder versions like `0.0.0-SNAPSHOT` in version files to make it clear they are placeholders that will be set during publish/build. The real version comes from git tags. Document this in `docs/versioning.md`.

**Notes**:
- Placeholder versions make it explicit that source files don't contain real versions
- Real versioning is driven by git tags
- Documentation needed in `docs/versioning.md`

---

### Q5: Version Increment Logic ✅ ANSWERED
**Question**: Should the workflow always increment patch version (4.0.0 → 4.0.1 → 4.0.2), or detect semantic changes (feat → minor, fix → patch)?

**Answer**: Patch-only increments (4.0.0 → 4.0.1 → 4.0.2). This is NOT real semver - we're just using semantic versioning format. Time-based versioning would be preferred but too much change right now. Document this clearly in `docs/versioning.md`.

**Notes**:
- Not real semver since no one depends on this app
- Time-based versioning preferred but deferred
- Document that we're using the format but not following semver rules

---

### Q6: Tag Format ✅ ANSWERED
**Question**: What format should git tags use? `v4.0.0` or `4.0.0`?

**Answer**: Use `v4.0.0` format (with `v` prefix), but ensure all scripts have normalization functions that can handle both formats (with and without `v`).

**Notes**:
- Tags will use `v4.0.0` format
- Scripts should normalize to handle both `v4.0.0` and `4.0.0` formats for robustness

---

### Q7: Migration Strategy for `develop` Branch ✅ ANSWERED
**Question**: How should we handle the existing `develop` branch? Rename it to `main`, merge it into `main`, or keep both temporarily?

**Answer**: 
1. Create `main` branch from current `develop` (which has 207 commits ahead of `master`)
2. Archive defunct branches: rename `master` → `archive/master` and `develop` → `archive/develop`
3. Update all workflows/docs to use `main`
4. Update default branch to `main`

**Notes**:
- `master` is stale (at v3.0.0 release, 207 commits behind `develop`)
- `develop` is the active branch with all current work
- Use `archive/` prefix for archived branches (common convention)
- Keeps history accessible while clearly marking them as archived

---

### Q8: GitHub Release Creation ✅ ANSWERED
**Question**: Should the workflow create GitHub releases automatically, or just tags?

**Answer**: Tags only for now. Can add GitHub release creation later if needed.

**Notes**:
- Tags are sufficient for version tracking
- Releases can be added later if needed

---

### Q9: Monorepo Integration ✅ ANSWERED
**Question**: Should the monorepo integration workflow (`trigger-monorepo-version-bump.yml`) be updated to trigger on `main` instead of `develop`?

**Answer**: Yes, update to trigger on `main` branch pushes.

**Notes**:
- Aligns with new branching model

---

### Q10: Version Script Location ✅ ANSWERED
**Question**: Where should the version tagging script live? `bin/` or `.github/scripts/`?

**Answer**: Use `bin/` directory to match existing OSMT conventions.

**Notes**:
- `bin/validate-versions.sh` already exists (validates version consistency across files)
- `bin/lib/common.sh` provides shared bash functions
- Consistent with existing script organization
