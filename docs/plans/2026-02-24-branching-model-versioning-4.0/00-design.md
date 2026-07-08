# Design: Branching Model & Versioning Migration to 4.0

## Scope of Work

Migration of OSMT from GitFlow (using `develop` branch) to a simplified workflow using `main` as the primary branch, updating to 4.0.0, and implementing automated post-merge version tagging.

## File Structure

```
osmt/
├── bin/
│   ├── tag-next-version.sh          # NEW: Tag next patch version on main
│   └── lib/
│       └── common.sh                # EXISTING
├── docs/
│   └── versioning.md                # NEW: Versioning documentation
├── .github/
│   └── workflows/
│       ├── ci.yml                    # UPDATE: develop → main
│       ├── main-push.yml            # NEW: Post-merge version tagging
│       └── trigger-monorepo-version-bump.yml  # UPDATE: develop → main
├── pom.xml                          # UPDATE: 0.0.0-SNAPSHOT
├── api/
│   └── pom.xml                      # UPDATE: 0.0.0-SNAPSHOT
├── ui/
│   └── package.json                 # UPDATE: 0.0.0-SNAPSHOT
└── CONTRIBUTING.md                   # UPDATE: Branching model docs
```

## Conceptual Architecture

```
Feature Branch → PR → Merge to main
                       │
                       ▼
              main-push.yml triggers
                       │
                       ▼
              bin/tag-next-version.sh
              - Read latest v4.0.X tag
              - Increment patch
              - Create & push new tag
                       │
                       ▼
              Git tags: v4.0.0, v4.0.1, ...
              (Version files stay 0.0.0-SNAPSHOT)
                       │
                       ▼
              Docker/build scripts use git tag version
```

## Main Components

1. **bin/tag-next-version.sh** - Reads latest v4.0.* tag, increments patch, creates and pushes new tag. Normalization for v prefix.
2. **.github/workflows/main-push.yml** - Triggers on push to main, runs tagging script, concurrency control.
3. **docs/versioning.md** - Documents strategy, placeholders, patch auto-increment, minor/major manual.
4. **Workflow updates** - ci.yml and trigger-monorepo-version-bump.yml change develop → main.
5. **Version files** - Set to 0.0.0-SNAPSHOT as placeholders.
