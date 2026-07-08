# Preserve collection description when converting Workspace to Collection

When converting a Workspace to a Collection, ensure the collection description is preserved.

[ClickUp: 868h2319c](https://app.clickup.com/t/868h2319c)

## Analysis

**Status: Fixed**

- `ConvertToCollectionComponent` overrode `updateObject()` with only `{ name, author, skills }` — **description was omitted**.
- Parent `CollectionFormComponent` includes `description` in `updateObject()` and in the form.
- The convert route has no collection `uuid`; the form was not loaded from the workspace.

**Root cause**: (1) `updateObject()` dropped `description`; (2) workspace name/description/author were not applied to the form on entry.

## Fix (2026-03-19)

- **`updateObject()`**: Include `description: formValues.description` in the payload for `createCollection`.
- **`ngOnInit`**: After `super.ngOnInit()`, call `getWorkspace()` and `patchValue` for `collectionName`, `description`, and `author` (author falls back to `AppConfig.settings.defaultAuthorValue` when missing).
- **Tests**: Spec stubs `getWorkspace`, asserts form patch and `updateObject().description`; convert route stub uses empty params (no fake collection uuid).
