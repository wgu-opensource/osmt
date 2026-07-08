# OSMT Versioning

## Overview

OSMT versioning is driven by git tags. Source files use placeholder versions;
the real version is determined at publish/build time from the current git tag.
This is not strict [semantic versioning](https://semver.org/)—no downstream
consumers depend on OSMT. Time-based versioning was considered but deferred.

## Version Format

Versions use the format `MAJOR.MINOR.PATCH` (e.g., 4.0.0). Git tags do not use
a prefix: `4.0.0`, `4.0.1`. Scripts should normalize to handle both formats in
case legacy tags or inputs include a `v` prefix.

## Placeholder Versions

Version files (`pom.xml`, `ui/package.json`) use `0.0.0-SNAPSHOT` as a
placeholder. This indicates the version is set at publish/build time. The real
version comes from git tags.

## Version Increments

- **Patch**: Automatic on each merge to `main` (4.0.0 → 4.0.1 → 4.0.2)
- **Minor**: Manual, done as needed
- **Major**: Manual, done as needed

Automated tagging is performed by `bin/tag-next-version.sh`, invoked by the
post-merge workflow on `main`.

## Docker and Build Scripts

Docker image tags and build artifacts use the version from the current git tag.
Scripts should extract the version from the tag (e.g., when building or
publishing images).

## Branching

See [CONTRIBUTING.md](CONTRIBUTING.md) for the branching workflow and how
`main` is used as the primary integration branch.
