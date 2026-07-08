# Phase 7: UI Unpublish All Button

## Scope of Phase

Add Unpublish All section to the sync management page. Show only when `allowUnpublishAll` is true. Add `unpublishAll()` to SyncService. Handle 202 and 503 responses.

## Code Organization Reminders

- Follow existing button patterns (Sync New Changes, Resync All).
- Use danger styling for destructive action.

## Implementation Details

### sync.service.ts

Add `allowUnpublishAll` to `SyncStateResponse`:

```typescript
export interface SyncStateResponse {
  integrations: SyncIntegrationDto[];
  allowUnpublishAll?: boolean;
}
```

Add method:

```typescript
unpublishAll(): Observable<string> {
  return this.http.post(`${this.base}/unpublish-all`, null, {
    headers: this.headers(),
    responseType: 'text',
  });
}
```

### sync-management.component.ts

Add state:

```typescript
unpublishing = false;
```

Add handler:

```typescript
onUnpublishAll(): void {
  if (!this.configured || this.syncing || this.resyncing || this.unpublishing)
    return;
  this.unpublishing = true;
  this.syncService.unpublishAll().subscribe({
    next: () => this.handleUnpublishSuccess(),
    error: err => this.handleUnpublishError(err),
  });
}

private handleUnpublishSuccess(): void {
  this.toastService.showToast('Success', 'Unpublish started.');
  this.unpublishing = false;
  this.loadState(() => this.startAutoRefresh());
}

private handleUnpublishError(err: { status?: number; error?: { message?: string } }): void {
  this.unpublishing = false;
  if (err?.status === 503) {
    this.toastService.showToast(
      'Unpublish not enabled',
      'Unpublish all is not enabled for this environment.',
      true
    );
  } else {
    this.handleError(err, 'Unpublish request failed');
  }
}
```

Include `unpublishing` in button disabled logic.

### sync-management.component.html

Add section (conditionally shown when `state?.allowUnpublishAll`):

```html
<section
  *ngIf="state?.allowUnpublishAll"
  class="l-syncSection t-margin-medium t-margin-bottom"
  aria-labelledby="unpublish-all-heading"
>
  <h2 id="unpublish-all-heading" class="t-type-heading2 t-margin-small t-margin-bottom">
    Unpublish All
  </h2>
  <p class="t-type-body t-margin-small t-margin-bottom">
    Remove all published skills and collections from the Credential Engine Registry.
    Use to clean up dev/test data. This cannot be undone.
  </p>
  <div class="l-syncActions">
    <button
      class="m-button m-button-resync-danger"
      type="button"
      [disabled]="syncing || resyncing || unpublishing"
      (click)="onUnpublishAll()"
    >
      {{ unpublishing ? 'Unpublishing...' : 'Unpublish All' }}
    </button>
  </div>
</section>
```

Reuse `m-button-resync-danger` for destructive styling (or add `m-button-unpublish-danger` if needed).

### sync-management.component.spec.ts

Add test for unpublish button visibility when allowUnpublishAll is true. Add test for 503 handling.

## Validate

```bash
cd ui && npm run build && npm run test -- --include='**/sync-management*'
```
