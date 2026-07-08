# Summary: Branching Model & Versioning Migration to 4.0

## Completed Work

1. **docs/versioning.md** – Versioning strategy documented (git tags, placeholders,
   patch auto-increment, minor/major manual)

2. **Version files** – Set to `0.0.0-SNAPSHOT` in `pom.xml`, `api/pom.xml`,
   `test/pom.xml`, `ui/package.json`

3. **bin/tag-next-version.sh** – Script to tag next patch version on main
   (v4.0.0, v4.0.1, …) with `normalize_version` helper

4. **.github/workflows/main-push.yml** – Post-merge workflow that runs
   tag-next-version.sh on push to main

5. **Workflow updates** – `ci.yml` and `trigger-monorepo-version-bump.yml`
   changed from `develop` to `main`

6. **CONTRIBUTING.md** – Branching model updated to main-based workflow

## Remaining (Manual)

- **Branch migration** – Create `main` from `develop`, archive `master` and
  `develop`, set GitHub default branch to `main`. See
  `07-branch-migration.md` for steps.
