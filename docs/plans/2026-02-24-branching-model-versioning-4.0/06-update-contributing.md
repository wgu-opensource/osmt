# Phase 6: Update CONTRIBUTING.md

## Scope of phase

Update `CONTRIBUTING.md` to document the new branching model (main as primary branch) instead of GitFlow with develop.

## Code Organization Reminders

- Preserve existing structure (Testing expectations, Commit message format, etc.)
- Replace only the Release / Branching Strategy section and related instructions
- Keep tone and style consistent

## Implementation Details

### 1. Release / Branching Strategy section
Replace GitFlow description with:
- Feature branches merge into `main` as the integration branch
- Branch from `origin/main` (not develop)
- No release branches; main is the release branch
- Post-merge: automatic patch version tagging on main

### 2. Using git with this project
- Change `git checkout origin/develop` to `git checkout origin/main`
- Change "branch from origin/develop" to "branch from origin/main"
- Update any other develop references in this section

### 3. PR instructions
- Update "merge into coming release" if needed
- PRs target `main`

### 4. Cross-reference
- Add link to `docs/versioning.md` for versioning details

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt

# No develop references in branching/contributing instructions
grep -n "develop" CONTRIBUTING.md
# Expected: zero matches (or only in "developer" if that word appears)

# main is referenced
grep -q "main" CONTRIBUTING.md

# versioning doc linked if added
grep -q "versioning" CONTRIBUTING.md
```
