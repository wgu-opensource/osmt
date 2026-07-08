# Credential Engine Sync – Implementation Notes (WGU)

Notes from WGU OSMT Use Case and CTDL Mapping and Project Tracker documents.

## Reference Documents

- **Registry Assistant API Handbook** – <https://credreg.net/registry/assistant>
- **WGU OSMT Use Case and CTDL Mapping** – OSMT fields to CTDL-ASN mapping
- **WGU Project Tracker – Action Items** – Publishing approach, API docs reference
- **Publishing Standalone Competencies: OSMT Upgrade Planning 2025** (Google Doc) – API documentation for standalone competency publish

## WGU Approach (from Project Tracker 2026-01-22)

1. **Competencies**: Publish each competency individually using "plain JSON" format whenever it is created or updated.
2. **Collections**: Publish collection including each competency CTID (`HasMember`) for competencies already published.
3. **Archiving**: Competency archived → mark as `PublicationStatusType: "Deprecated"`; Collection archived → `LifeCycleStatusType: "Ceased"`.
4. **Events**: Creation, update, deleted, archived → sync result to Registry.

## OSMT → CTDL Mapping (from WGU CTDL Mapping doc)

| OSMT Field     | CTDL-ASN Property   | Notes                           |
| -------------- | ------------------- | ------------------------------- |
| skillName      | competencyLabel     | Short name/label                |
| author         | author              | Creator of intellectual content |
| skillStatement | competencyText      | Applied capabilities/behaviors  |
| category       | competencyCategory  | Broad group of related RSDs     |
| keywords       | conceptKeyword      | Search terms                    |
| standards      | conceptKeyword      | Map to keywords (acronyms)      |
| creator        | creator             | Source of the skill             |
| certifications | (do not publish)    | Acronyms too ambiguous          |
| employers      | (do not publish)    | Subjective, not validated       |
| occupations    | occupationType      | SOC/O\*NET codes                |
| Collection     | CompetencyFramework | Per WGU example                 |

## Example: WGU Cybersecurity Collection

Published as **Competency Framework** (not standalone competencies) to CE sandbox:

- [Finder view](https://sandbox.credentialengine.org/finder/collection/ce-3e7df7ec-1a9b-4503-9ff3-21256022b515/)
- Via CaSS integrated with CE publishing stack (not Registry Assistant directly)

## Competency Publish Workflow

The handbook describes two patterns:

1. **Competency Framework** – Competencies published as part of a framework, with `IsPartOf` pointing to framework CTID.
2. **Standalone / Collection** – Registry Assistant added standalone competency publish (2/14/2026). WGU planning doc has API details.

**Resolved**: The standalone competency endpoint (`/assistant/competency/publish`) expects a `CompetencyListRequest` with a `Competencies` array, not a `CompetencyRequest` with a single `Competency` object. The CTID must also be `ce-{valid-UUID}`. See [PublishStandaloneCompetencies.cs](https://github.com/CredentialEngine/Registry_Assistant/blob/master/src/SamplePublishing/RA.SamplesForDocumentation/CompetencyFrameworks/PublishStandaloneCompetencies.cs) for the reference sample and [credreg.net Registry Assistant](https://credreg.net/registry/assistant) for full API documentation.

## CE Contacts

- **Michael Parsons** – technical team at CE, responsible for the publishing API
- **Jeanne Kitchens** – Credential Engine CTSO
