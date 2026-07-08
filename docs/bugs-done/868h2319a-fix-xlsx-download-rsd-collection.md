# Fix XLSX download for RSD or Collection

Ensure that downloading an RSD or a Collection as XLSX works properly.

[ClickUp: 868h2319a](https://app.clickup.com/t/868h2319a)

## Analysis

**Status: Fixed**

- **RSD XLSX**: `ExportRsdComponent.getRsdXlsx()` uses `exportSearchXlsx` with `ApiSearch({ uuids: [uuid] })`, then polls `getResultExportedXlsxLibrary`. Task-based export flow.
- **Collection XLSX**: `ExportCollectionComponent.getCollectionXlsx()` uses `requestCollectionSkillsXlsx` and polls `getXlsxTaskResultsIfComplete` → `results/media/{uuid}` (already public).
- **Root cause (RSD / search / library XLSX)**: `ExportSkillsToXlsxTask` used `TASK_DETAIL_BATCH` for `apiResultPath`. Spring secures GET `results/batch` as authenticated only, while GET `results/media` is `permitAll`. Anonymous users on public RSD pages got **401** when polling the completed export.
- Collection XLSX used `XlsxTask` with `TASK_DETAIL_MEDIA` all along; no change required there.

## Fix (2026-03-19)

- **API**: `ExportSkillsToXlsxTask.apiResultPath` now uses `TASK_DETAIL_MEDIA` (same as `XlsxTask`).
- **Test**: `TaskResultExportXlsxTest` asserts the task result URI is `/api/v3/results/media/{uuid}`.
- **UI**: Removed debug `console.log` from `getResultExportedXlsxLibrary`.

## Follow-up (2026-03-19)

- **Security**: `POST /api/v3/export/skills/csv` and `.../xlsx` were still blocked for anonymous users by the `/api/**` role rule before the controller ran. Added `permitAll()` for those routes in `SecurityConfigHelper.configurePublicEndpoints`; `RichSkillController` continues to enforce `allowPublicSearching` and publish-status filters for anonymous callers.
