# Announcing the release of OSMT 2.5.0
February 28, 2023
## Enhancements
OSMT 2.5.0 brings 2 new feature enhancements

- Addition of a "My Workspace" feature, allowing a collection-like place for a user to save and sort RSDs. RSDs can be added to and removed from the Workspace, and the Workspace can be converted to a real Collection, and exported as a CSV without creating a real Collection. The Workspace can also be reset (emptied).

- Collections now have a "description" field. The description is indexed and searchable.

- An RSD now has a "Copy Public URL" button, to simplify copying the canonical URL for that RSD.

## Configuration Changes
> **Warning**
> 2.5.0 has database changes that will be applied by Flyway, if that is enabled.
> We recommend destroying ElasticSearch storage and recreating / reindexing.

------------------------------------------------------------------------
# Announcing the release of OSMT 2.4.2
January 27, 2023

> # This release enables OSMT roles by default.
> **Warning**
> See [Role-based Access in OSMT](https://github.com/wgu-opensource/osmt/blob/develop/README.md#role-based-access-in-osmt) in the main README.md for configuration details.

# Enhancements
* OSMT users can now export selected search result RSDs as CSV.
* OSMT admins can now export draft Collections as CSV
* OSMT admins can now delete any Collection

# Configuration changes
> **Warning**
* This release enables OSMT roles by default.
  See [Role-based Access in OSMT](https://github.com/wgu-opensource/osmt/blob/develop/README.md#role-based-access-in-osmt) in the main README.md for configuration details.

> **Warning**
* This release removes all OSMT application*.properties file from the osmt-api-lib Maven artifact. This library allows OSMT Spring endpoints to operate while added as a dependency within another Spring Boot application.

**Full Changelog**: https://github.com/wgu-opensource/osmt/compare/2.4.1...2.4.2

------------------------------------------------------------------------
# Announcing the release of OSMT 2.3.0
January 6, 2023

This release brings an upgrade to Kotlin, from 1.5.10 to 1.7.21. This release also includes many npm dependency upgrades.

## Defect Fixes
* Corrected an issue with secondary RSD sort order.

**Full Changelog**: https://github.com/wgu-opensource/osmt/compare/2.2.0...2.3.0

------------------------------------------------------------------------
# Announcing the release of OSMT 2.2.0
December 16, 2022

This release brings several defect fixes, and completes the feature enhancement for roles within OSMT (admin, curator, viewer).

## Deploying this release will require a delete and reindex for the supporting ElasticSearch indices. See [Reindex after changes to Elasticsearch @Document index classes](https://github.com/wgu-opensource/osmt/blob/develop/api/README.md#reindex-after-changes-to-elasticsearch-document-index-classes) in the API module README.md for guidance.

## Defect fixes:
* RSD results are now presented in a consistent sort order by category and name, case insensitive. Previously, when results where sorted by category (the default), the RSDs were correctly sorted by category, but therein not sorted consistently. This would sometimes cause an RSD to appear on multiple pages in the results. When paging through results to add RSDs to collections, a given RSD might be shown twice, or not be shown at all. All previous sorting was done case sensitive, so "AWS" would appear before "Abstract".
* A quoted search terms will now return RSDs matching that exact complete term. Previously, searching "Communications" in Category would return all RSDs containing that term, including both "Communications" and "Argumentative and Alternative Communications". When quoting the search term, the expected behavior is to only return RSDs with the exact category "Communications", and not  others like "Argumentative and Alternative Communications".

## Enhancements
* The complete RSD library can now be exported as CSV by OSMT users with the Admin role. If the instance is deployed with roles disabled, then any authenticated user can export the library.

## Patching and Developer Tooling:
* Base Docker images were upgraded to oraclelinux:9, as CentOS has been retired.
* Added documentation supporting an LDD for OSMT. See [here](https://github.com/wgu-opensource/osmt/tree/develop/docs/arch) for more details.

------------------------------------------------------------------------

# Announcing the release of OSMT 2.1.0
Novemeber 4, 2022

This release brings several defect fixes, and completes the feature enhancement for roles within OSMT (admin, curator, viewer).

## Defect fixes:
- RSD result counts greater than 10,000 are now accurately reports in the UI
- BLS and O*NET job code hierarchies have been corrected.
  - This was an import issue, and to benefit from this fix, you will need to reimport the 2018 BLS Job Code metadata, and reindex your ElasticSearch instance. See https://github.com/wgu-opensource/osmt/discussions/251 for more details about using this fix.
- The back button now returns from an RSD detail to the originating search results. Previously, the back button return the user to the RSD full library.
- Resolved issue where all result RSD were added to a collection (instead of just the selected RSDs)
- Fixed a field mapping for CollectionDoc. This would have resulted in nulls in JSON

## Enhancements:
- Completed UI implementation of role-based access in OSMT. See "Role-based Access in OSMT" in README.md

## Patching and Developer Tooling:
- Adjusted/upgraded OpenCSV and Jackson DataBind dependencies to address critical vulnerabilities
  - CVE-2022-42889
  - CVE-2022-42004
- Added development as default local Angular configuration
  - The Maven build continues to run a production Angular build, but the npm commands now default to development

------------------------------------------------------------------------
# Announcing OSMT Release 2.0.0
July 22, 2022

The main changes in this release are in packaging and distribution.

* OSMT jar artifacts are now publicly available on Maven Central. Please note that the UI jar is empty and has no purpose, as the UI Angular application is embedded in the Spring Boot jar. A coming OSMT release will distribute the UI using typical JavaScript package manager tools.

* This release unifies an internal WGU fork of OSMT code with the open source code base. The Spring Boot API endpoints can now be used as a dependency in another Spring application (which was an internal requirement for WGU's operational standards). The API module builds the typical repackaged Spring Boot application jar, but also builds a normal Java jar with a "lib" classifier (osmt-api-lib). You can declare this as a Maven dependency in an independent Spring application. This osmt-api-lib artifact has certain exclusions, including the embedded Angular UI files. See api/pom.xml for the specifics.

------------------------------------------------------------------------
# Announcing OSMT Release 1.1.0
January 25, 2022

### Notable Changes in Release 1.1.0:
This release is focused on preparing OSMT for public contributors, with some feature enhancements.
#### Existing OSMT Instances - delete and rebuild ElasticSearch index
* Because of internal application framework upgrades, existing OSMT instances that upgrade to version 1.1.0 will need to delete and reindex ElasticSearch indices. See [Reindex after changes to Elasticsearch @Document index classes](https://github.com/wgu-opensource/osmt/blob/develop/api/README.md#reindex-after-changes-to-elasticsearch-document-index-classes) for details. You can also follow discussions on this topic [here](https://github.com/wgu-opensource/osmt/discussions/141).
#### Features and Enhancements:
- added support for anonymous API searching
- added support for filtering by UUID when searching for skills
#### Development Tooling:
- added OSMT CLI to simplify local development and evaluation, and assist in Discussion forum support.
- added more complete support for a Quickstart evaluation config and a Development configuration, with more clear support for Angular/webpack servers as a front-end proxy
- added official OSMT app Docker image to DockerHub
  - (https://hub.docker.com/repository/docker/wguopensource/osmt-app)
- upgraded to Spring Boot 2.5.5 / Kotlin 1.5.10
- upgraded log4j-related dependencies
- updated OpenAPI specs files
- added many documentation and development tooling refinements

------------------------------------------------------------------------
# Announcing OSMT Release 1.0.5
October 21, 2021

## What's Changed
* Fixed bug: '1 collection' was sometimes '1 collections' (found through new test) by @drey-bigney in https://github.com/wgu-opensource/osmt/pull/3
* added a new file called license by @wgu-edwin in https://github.com/wgu-opensource/osmt/pull/4
* Created code of conduct file by @wgu-edwin in https://github.com/wgu-opensource/osmt/pull/6
* Contribution by @wgu-edwin in https://github.com/wgu-opensource/osmt/pull/7
* Feature/multiple alignments by @coffindragger in https://github.com/wgu-opensource/osmt/pull/8
* Final repo check by @wgu-edwin in https://github.com/wgu-opensource/osmt/pull/11
* Fixed Junit test case failure. by @karan-beyond in https://github.com/wgu-opensource/osmt/pull/13
* added a clarifying statement under attribution by @wgu-edwin in https://github.com/wgu-opensource/osmt/pull/17
* Update CONTRIBUTING.md by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/22
* DMND-631 - Update code to include BLS JobCodes when the skills are im… by @Roberto-Meza in https://github.com/wgu-opensource/osmt/pull/26
* DMND-673 Update open source repo with osmt tests by @Roberto-Meza in https://github.com/wgu-opensource/osmt/pull/29
* DMND-674 Update open source repo with osmt-ui tests by @Roberto-Meza in https://github.com/wgu-opensource/osmt/pull/31
* Test improvements by @hikr17 in https://github.com/wgu-opensource/osmt/pull/32
* Bug fixes by @hikr17 in https://github.com/wgu-opensource/osmt/pull/33
* Add noauth profile config for local development by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/34
* Update readme by @wgu-edwin in https://github.com/wgu-opensource/osmt/pull/9
* Closes #18 - Update README for community contributors by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/39
* Add Testing Expections to CONTRIBUTING.md by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/43
* Adding test for ElasticSearchReindexer by @Roberto-Meza in https://github.com/wgu-opensource/osmt/pull/45
* Fix issues with docker-compose by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/41
* Feature/add code coverage by @hikr17 in https://github.com/wgu-opensource/osmt/pull/46
* Allow anonymous API acess to search and list endpoints for published skills and collections by @coffindragger in https://github.com/wgu-opensource/osmt/pull/16
* Revert "Allow anonymous API acess to search and list endpoints for published skills and collections" by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/53
* update Maven dependency by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/54
* update config changes for dev docker-compose deployment by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/51
* update README for Quickstart configs by @JohnKallies in https://github.com/wgu-opensource/osmt/pull/56
* Changes for import functionality. by @karan-beyond in https://github.com/wgu-opensource/osmt/pull/60

## New Contributors
* @drey-bigney made their first contribution in https://github.com/wgu-opensource/osmt/pull/3
* @wgu-edwin made their first contribution in https://github.com/wgu-opensource/osmt/pull/4
* @coffindragger made their first contribution in https://github.com/wgu-opensource/osmt/pull/8
* @karan-beyond made their first contribution in https://github.com/wgu-opensource/osmt/pull/13
* @JohnKallies made their first contribution in https://github.com/wgu-opensource/osmt/pull/22
* @Roberto-Meza made their first contribution in https://github.com/wgu-opensource/osmt/pull/26
* @hikr17 made their first contribution in https://github.com/wgu-opensource/osmt/pull/32

**Full Changelog**: https://github.com/wgu-opensource/osmt/commits/1.0.5
