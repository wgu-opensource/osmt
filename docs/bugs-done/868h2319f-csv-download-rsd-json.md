# Fix CSV download for RSD (should not download JSON)

Ensure that the 'Download RSD in CSV format' option results in a CSV file download, not a JSON file.

[ClickUp: 868h2319f](https://app.clickup.com/t/868h2319f)

## Analysis

**Status: Fixed**

- `getSkillCsvByUuid` in `ui/src/app/richskill/service/rich-skill.service.ts` called `skills/{uuid}` with **Accept: 'application/json'** (line 90).
- That hit the JSON skill-detail endpoint, which returns JSON. The response was saved with a `.csv` filename but contained JSON.
- API had `byUUIDCsvView` at `collections/{uuid}/updateSkills` (RichSkillCsvExport) — wrong path for the UI; no GET `/api/v3/skills/{uuid}` produced `text/csv`.

**Root cause**: Frontend requested JSON and got JSON; the filename extension was misleading.

**Fix**: Either (1) Add `Accept: 'text/csv'` and a skill-detail CSV endpoint on the API, or (2) Use the bulk export task flow (like XLSX) for single-skill CSV.

## Fix (2026-03-19)

- **API**: Added `byUUIDCsvV3` and `byUUIDCsvV2` `GET` mappings on skill detail paths with `produces = text/csv`, sharing `skillDetailCsvEntity` with legacy `byUUIDCsvView` on `collections/.../updateSkills`.
- **UI**: `getSkillCsvByUuid` sends `Accept: text/csv` so content negotiation returns CSV from `/api/v3/skills/{uuid}`.
- **Tests**: `RichSkillControllerTest.testByUUIDCsvV3OnSkillDetailPath`; `rich-skill.service.spec` expects `Accept: text/csv`.
