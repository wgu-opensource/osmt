# Extensive Type-Switch Duplication in Batch Loop

**Severity:** Low (code quality)
**File:** `SyncService.kt` — `doSyncSinceWatermark()` (lines 170–433)

## Problem

`doSyncSinceWatermark` is ~260 lines. The `when (recordType)` pattern repeats **8 times** inside the while loop:

1. Fetching the batch (lines 179–191)
2. Debug-logging the raw batch (lines 194–220)
3. Cursor-filtering the batch (lines 222–257)
4. Extracting max watermark from a filtered-empty batch (lines 267–294)
5. Deduplicating the batch (lines 323–358)
6. Processing the batch (lines 359–386)
7. Extracting max watermark from the processed batch (lines 392–415)
8. An unreachable `else` branch each time

Each branch differs only by `RichSkillDescriptorDao` vs `CollectionDao` and `it.updateDate` vs `it.updateDate` (same field name, different type). The logic is identical.

## Impact

- Hard to verify correctness — a bug fix in one branch can be missed in the other.
- The skill and collection branches could silently diverge.
- Makes the method difficult to review and reason about.

## Suggestion

Extract the common batch loop into a generic function parameterized by a simple interface:

```kotlin
interface SyncBatchItem {
    val itemId: Long
    val itemUuid: String
    val itemUpdateDate: LocalDateTime
}
```

Then `doSyncSinceWatermark` becomes a single loop without `when` branches. The `processSkillBatch` and `processCollectionBatch` methods are already separate, so the per-record processing is already factored out — only the cursor/filter/dedup logic needs unification.

This is not blocking for merge but would significantly improve maintainability for future sync changes.
