# Phase 7: Branch Migration Steps

## Scope of phase

Document and execute (or prepare for manual execution) the branch migration: create `main` from `develop`, archive `master` and `develop`, update GitHub default branch.

## Code Organization Reminders

- These steps are primarily repo-level operations
- May require GitHub UI or API for default branch change
- Document clearly for reproducibility

## Implementation Details

### 1. Pre-requisites
- All planned changes (phases 1-6) merged to `develop`
- No blocking open PRs (or coordinate with team)
- Ensure you have push access and default branch change permissions

### 2. Local execution (from develop branch)
```bash
cd /Users/yona/dev/skybridge/osmt

# Ensure on develop and up to date
git checkout develop
git pull origin develop

# Create main from current develop
git checkout -b main
git push -u origin main

# Archive master: push to archive/master
git push origin origin/master:archive/master

# Archive develop: push to archive/develop
git push origin origin/develop:archive/develop

# Delete original master and develop (optional - do after default branch is main)
# git push origin --delete master
# git push origin --delete develop
```

### 3. GitHub Settings
- Go to Settings → General → Default branch
- Change default branch from `develop` to `main`
- GitHub may require main to exist first (step 2)

### 4. Order of operations (recommended)
1. Push `main` branch (create from develop)
2. In GitHub: set default branch to `main`
3. Push `archive/master` and `archive/develop`
4. Delete `master` and `develop` (optional, after verification period)

### 5. Create initial tag
- After main exists and first merge lands, the main-push workflow will create `v4.0.0` (or next patch)
- If no v4.0.* tags exist, script should use v4.0.0 as first tag

### 6. Verification
- CI runs on PRs to main
- main-push runs on push to main
- Monorepo trigger runs on push to main

## Validate

```bash
# After migration:
git fetch origin
git branch -a | grep -E "main|archive"

# Default branch is main (check via gh cli or GitHub UI)
gh repo view --json defaultBranchRef 2>/dev/null | grep -q main || true
```

## Notes
- Phase may be executed manually by maintainers
- Consider doing migration in a separate session from code changes
- Keep archive branches for at least a verification period before optional deletion
