import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  SyncIntegrationDto,
  SyncService,
  SyncStateResponse,
} from './sync.service';
import { ToastService } from '../../toast/toast.service';
import { SvgIcon } from '../../core/SvgHelper';

interface SyncStatusDisplay {
  label: string;
  correlationId: string | null;
}

@Component({
  selector: 'app-sync-management',
  templateUrl: './sync-management.component.html',
  styleUrls: ['./sync-management.component.scss'],
})
export class SyncManagementComponent implements OnInit, OnDestroy {
  state: SyncStateResponse | null = null;
  configured = true;
  loading = true;
  syncing = false;
  resyncing = false;
  unpublishing = false;
  autoRefreshUntilDone = false;
  private refreshIntervalId: ReturnType<typeof setInterval> | null = null;
  private readonly refreshIntervalMs = 5000;
  private readonly maxRefreshDurationMs = 60 * 60 * 1000; // 1 hour

  readonly syncNewChangesIcon = `/assets/images/svg-defs.svg#${SvgIcon.UPLOAD}`;
  readonly resyncAllIcon =
    '/assets/images/svg-extra-defs.svg#icon-refresh-sync';

  constructor(
    private syncService: SyncService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadState(() => this.startAutoRefresh());
  }

  ngOnDestroy(): void {
    this.clearRefreshInterval();
  }

  loadState(afterSuccess?: () => void, afterError?: () => void): void {
    this.loading = true;
    this.syncService.getState().subscribe({
      next: res => {
        this.state = res;
        this.configured = true;
        this.loading = false;
        afterSuccess?.();
      },
      error: err => {
        this.loading = false;
        if (err?.status === 503) {
          this.configured = false;
          this.state = null;
        } else if (err?.status === 401 || err?.status === 403) {
          this.toastService.showToast(
            'Error',
            err?.error?.message ?? 'Unauthorized. Please log in again.',
            true
          );
        } else {
          this.toastService.showToast(
            'Error',
            'Failed to load sync state',
            true
          );
        }
        afterError?.();
      },
    });
  }

  getStatusDisplay(i: SyncIntegrationDto): SyncStatusDisplay {
    if (!i.statusJson) return { label: '—', correlationId: null };
    try {
      const s = JSON.parse(i.statusJson);
      const correlationId =
        s?.sessionCorrelationId ?? s?.error?.correlationId ?? null;
      if (s?.error) {
        return {
          label: `Error: ${s.error.message ?? 'Unknown'}`,
          correlationId,
        };
      }
      if (s?.inProgress === true) {
        return { label: 'In progress…', correlationId };
      }
      const batches = s?.batchesCompleted;
      const label = batches != null ? `Ok (${batches} batches)` : 'Ok';
      return { label, correlationId };
    } catch {
      return { label: '—', correlationId: null };
    }
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(
      () => this.toastService.showToast('Copied', 'Correlation ID copied'),
      () =>
        this.toastService.showToast(
          'Copy failed',
          'Could not copy to clipboard',
          true
        )
    );
  }

  isSyncDone(): boolean {
    if (!this.state?.integrations?.length) return true;
    return this.state.integrations.every(i => {
      if (!i.statusJson) return true;
      try {
        const s = JSON.parse(i.statusJson);
        if (s?.error) return true;
        return s?.inProgress !== true;
      } catch {
        return true;
      }
    });
  }

  hasSyncError(): boolean {
    if (!this.state?.integrations?.length) return false;
    return this.state.integrations.some(i => {
      if (!i.statusJson) return false;
      try {
        const s = JSON.parse(i.statusJson);
        return !!s?.error;
      } catch {
        return false;
      }
    });
  }

  get summaryStatus(): 'up-to-date' | 'in-progress' | 'error' | 'never-synced' {
    if (!this.state?.integrations?.length) return 'never-synced';
    if (this.hasSyncError()) return 'error';
    if (!this.isSyncDone()) return 'in-progress';
    const hasAnyWatermark = this.state.integrations.some(
      i => i.syncWatermark != null
    );
    return hasAnyWatermark ? 'up-to-date' : 'never-synced';
  }

  get totalPendingCount(): number {
    if (!this.state?.integrations?.length) return 0;
    return this.state.integrations.reduce(
      (sum, i) => sum + (i.pendingCount ?? 0),
      0
    );
  }

  get summaryError(): SyncStatusDisplay | null {
    const withError = this.state?.integrations?.find(i => {
      try {
        return !!JSON.parse(i.statusJson ?? '{}')?.error;
      } catch {
        return false;
      }
    });
    return withError ? this.getStatusDisplay(withError) : null;
  }

  get lastSyncedDisplay(): Date | null {
    if (!this.state?.integrations?.length) return null;
    const dates = this.state.integrations
      .filter(i => i.syncWatermark != null)
      .map(i => new Date(i.syncWatermark!).getTime())
      .filter(t => !isNaN(t));
    if (dates.length === 0) return null;
    return new Date(Math.max(...dates));
  }

  get pendingByRecordType(): { skills: number; collections: number } {
    if (!this.state?.integrations?.length) {
      return { skills: 0, collections: 0 };
    }
    let skills = 0;
    let collections = 0;
    for (const i of this.state.integrations) {
      const count = i.pendingCount ?? 0;
      if (i.recordType === 'skill') skills = count;
      else if (i.recordType === 'collection') collections = count;
    }
    return { skills, collections };
  }

  private clearRefreshInterval(): void {
    if (this.refreshIntervalId != null) {
      clearInterval(this.refreshIntervalId);
      this.refreshIntervalId = null;
    }
  }

  private startAutoRefresh(): void {
    if (!this.autoRefreshUntilDone && this.isSyncDone()) return;
    this.clearRefreshInterval();
    const startTime = Date.now();
    this.refreshIntervalId = setInterval(() => {
      this.syncService.getState().subscribe({
        next: res => {
          this.state = res;
          if (this.isSyncDone()) {
            this.clearRefreshInterval();
            this.syncing = false;
            this.resyncing = false;
            this.unpublishing = false;
            if (this.hasSyncError()) {
              this.toastService.showToast(
                'Sync finished',
                'Sync completed with errors. Check status.',
                true
              );
            } else {
              this.toastService.showToast(
                'Sync finished',
                'Sync completed successfully.'
              );
            }
          } else if (Date.now() - startTime > this.maxRefreshDurationMs) {
            this.clearRefreshInterval();
            this.syncing = false;
            this.resyncing = false;
            this.unpublishing = false;
            this.toastService.showToast(
              'Auto refresh stopped',
              'Stopped after 1 hour. Sync may still be running.',
              true
            );
          }
        },
        error: () => {
          this.clearRefreshInterval();
          this.syncing = false;
          this.resyncing = false;
          this.unpublishing = false;
        },
      });
    }, this.refreshIntervalMs);
  }

  private handleSyncSuccess(toastMsg: string): void {
    this.toastService.showToast('Success', toastMsg);
    this.loadState(
      () => {
        if (!this.autoRefreshUntilDone) {
          this.syncing = false;
          this.resyncing = false;
        }
        this.startAutoRefresh();
      },
      () => {
        this.syncing = false;
        this.resyncing = false;
      }
    );
  }

  onSyncNow(): void {
    if (!this.configured || this.syncing || this.resyncing || this.unpublishing)
      return;
    this.syncing = true;
    this.syncService.syncAll().subscribe({
      next: () => this.handleSyncSuccess('Sync new changes started.'),
      error: err => this.handleSyncError(err),
    });
  }

  onResync(): void {
    if (!this.configured || this.syncing || this.resyncing || this.unpublishing)
      return;
    this.resyncing = true;
    this.syncService.resyncAll().subscribe({
      next: () => this.handleSyncSuccess('Full resync started.'),
      error: err => this.handleResyncError(err),
    });
  }

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
    this.loadState(
      () => this.startAutoRefresh(),
      () => {
        this.unpublishing = false;
      }
    );
  }

  private handleUnpublishError(err: {
    status?: number;
    error?: { message?: string };
  }): void {
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

  private handleSyncError(err: {
    status?: number;
    error?: { message?: string };
  }): void {
    this.syncing = false;
    this.handleError(err, 'Sync request failed');
  }

  private handleResyncError(err: {
    status?: number;
    error?: { message?: string };
  }): void {
    this.resyncing = false;
    this.handleError(err, 'Resync request failed');
  }

  private handleError(
    err: { status?: number; error?: { message?: string } },
    fallback: string
  ): void {
    if (err?.status === 503) {
      this.configured = false;
      this.loadState();
    } else if (err?.status === 401 || err?.status === 403) {
      const msg = err?.error?.message ?? 'Unauthorized. Please log in again.';
      this.toastService.showToast('Error', msg, true);
    } else {
      const msg =
        typeof err?.error === 'object' && err?.error?.message
          ? err.error.message
          : fallback;
      this.toastService.showToast('Error', msg, true);
    }
  }
}
