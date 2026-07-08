import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { SyncManagementComponent } from './sync-management.component';
import { SyncService } from './sync.service';
import { ToastService } from '../../toast/toast.service';
import { AppConfig } from '../../app.config';
import { SystemMessageComponent } from '../../core/system-message.component';
import { AccordianComponent } from '../../core/accordian.component';
import { AuthService } from '../../auth/auth-service';
import { AuthServiceStub } from '../../../../test/resource/mock-stubs';

describe('SyncManagementComponent', () => {
  let component: SyncManagementComponent;
  let fixture: ComponentFixture<SyncManagementComponent>;
  let syncService: SyncService;
  let toastService: ToastService;

  beforeEach(async () => {
    AppConfig.settings = { baseApiUrl: 'http://localhost:8080' } as never;

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, CommonModule, FormsModule],
      declarations: [
        SyncManagementComponent,
        SystemMessageComponent,
        AccordianComponent,
      ],
      providers: [
        SyncService,
        ToastService,
        { provide: AuthService, useClass: AuthServiceStub },
      ],
    })
      .overrideComponent(SyncManagementComponent, { set: { providers: [] } })
      .compileComponents();

    fixture = TestBed.createComponent(SyncManagementComponent);
    component = fixture.componentInstance;
    syncService = TestBed.inject(SyncService);
    toastService = TestBed.inject(ToastService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads state on init', () => {
    const state = {
      integrations: [
        {
          syncKey: 'default',
          recordType: 'skill',
          syncWatermark: '2025-01-01',
        },
      ],
    };
    spyOn(syncService, 'getState').and.returnValue(of(state));

    component.loadState();

    expect(component.state).toEqual(state);
    expect(component.configured).toBe(true);
  });

  it('getStatusDisplay parses statusJson and shows error with correlationId', () => {
    const display = component.getStatusDisplay({
      syncKey: 'default',
      recordType: 'skill',
      syncWatermark: null,
      statusJson: JSON.stringify({
        error: {
          message: 'Connection refused',
          correlationId: 'abc12def34',
        },
      }),
    });
    expect(display.label).toBe('Error: Connection refused');
    expect(display.correlationId).toBe('abc12def34');
  });

  it('getStatusDisplay returns Ok with batches when no error', () => {
    const display = component.getStatusDisplay({
      syncKey: 'default',
      recordType: 'skill',
      syncWatermark: '2025-01-01',
      statusJson: JSON.stringify({ batchesCompleted: 3 }),
    });
    expect(display.label).toBe('Ok (3 batches)');
    expect(display.correlationId).toBeNull();
  });

  it('getStatusDisplay shows sessionCorrelationId for log search', () => {
    const display = component.getStatusDisplay({
      syncKey: 'default',
      recordType: 'skill',
      syncWatermark: '2025-01-01',
      statusJson: JSON.stringify({
        batchesCompleted: 2,
        sessionCorrelationId: 'xyz99abc12',
      }),
    });
    expect(display.label).toBe('Ok (2 batches)');
    expect(display.correlationId).toBe('xyz99abc12');
  });

  it('getStatusDisplay shows In progress when inProgress is true', () => {
    const display = component.getStatusDisplay({
      syncKey: 'default',
      recordType: 'skill',
      syncWatermark: null,
      statusJson: JSON.stringify({
        inProgress: true,
        sessionCorrelationId: 'abc123',
      }),
    });
    expect(display.label).toBe('In progress…');
    expect(display.correlationId).toBe('abc123');
  });

  it('sets configured false on 503', () => {
    spyOn(syncService, 'getState').and.returnValue(
      throwError(() => ({ status: 503 }))
    );

    component.loadState();

    expect(component.configured).toBe(false);
  });

  it('onSyncNow calls syncAll when configured', () => {
    component.state = { integrations: [] };
    component.configured = true;
    component.loading = false;
    spyOn(syncService, 'syncAll').and.returnValue(of('ok'));
    spyOn(syncService, 'getState').and.returnValue(of({ integrations: [] }));
    spyOn(toastService, 'showToast');

    component.onSyncNow();

    expect(syncService.syncAll).toHaveBeenCalled();
    expect(toastService.showToast).toHaveBeenCalledWith(
      'Success',
      'Sync new changes started.'
    );
  });

  it('onResync calls resyncAll when configured', () => {
    component.state = { integrations: [] };
    component.configured = true;
    component.loading = false;
    spyOn(syncService, 'resyncAll').and.returnValue(of('ok'));
    spyOn(syncService, 'getState').and.returnValue(of({ integrations: [] }));
    spyOn(toastService, 'showToast');

    component.onResync();

    expect(syncService.resyncAll).toHaveBeenCalled();
    expect(toastService.showToast).toHaveBeenCalledWith(
      'Success',
      'Full resync started.'
    );
  });

  it('shows error toast on 401 from syncAll', () => {
    component.state = { integrations: [] };
    component.configured = true;
    component.syncing = false;
    spyOn(syncService, 'syncAll').and.returnValue(
      throwError(() => ({ status: 401, error: { message: 'Unauthorized' } }))
    );
    spyOn(toastService, 'showToast');

    component.onSyncNow();

    expect(toastService.showToast).toHaveBeenCalledWith(
      'Error',
      'Unauthorized',
      true
    );
  });

  it('renders sections and column help when configured', () => {
    const state = {
      integrations: [
        {
          syncKey: 'default',
          recordType: 'skill',
          syncWatermark: '2025-01-01',
        },
      ],
    };
    spyOn(syncService, 'getState').and.returnValue(of(state));
    fixture = TestBed.createComponent(SyncManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    const html = fixture.nativeElement as HTMLElement;
    expect(html.textContent).toContain('Technical Details');
    expect(html.textContent).toContain('Sync New Changes');
    expect(html.textContent).toContain('Resync All');
    expect(html.textContent).toContain('Integration Key');
    expect(html.textContent).toContain('Watermark');
  });

  it('summaryStatus returns up-to-date when done and no pending', () => {
    component.state = {
      integrations: [
        {
          syncKey: 'default',
          recordType: 'skill',
          syncWatermark: '2025-01-01',
          pendingCount: 0,
        },
      ],
    };
    expect(component.summaryStatus).toBe('up-to-date');
    expect(component.totalPendingCount).toBe(0);
  });

  it('summaryStatus returns error when integration has error', () => {
    component.state = {
      integrations: [
        {
          syncKey: 'default',
          recordType: 'skill',
          syncWatermark: '2025-01-01',
          statusJson: JSON.stringify({ error: { message: 'Failed' } }),
        },
      ],
    };
    expect(component.summaryStatus).toBe('error');
    expect(component.summaryError?.label).toContain('Failed');
  });

  it('summaryStatus returns in-progress when integration inProgress', () => {
    component.state = {
      integrations: [
        {
          syncKey: 'default',
          recordType: 'skill',
          syncWatermark: null,
          statusJson: JSON.stringify({ inProgress: true }),
        },
      ],
    };
    expect(component.summaryStatus).toBe('in-progress');
  });

  it('clears syncing and resyncing when loadState completes without autoRefresh', () => {
    component.syncing = true;
    component.resyncing = true;
    component.autoRefreshUntilDone = false;
    spyOn(syncService, 'getState').and.returnValue(of({ integrations: [] }));

    component.loadState(
      () => {
        if (!component.autoRefreshUntilDone) {
          component.syncing = false;
          component.resyncing = false;
        }
      },
      () => {}
    );

    expect(component.syncing).toBe(false);
    expect(component.resyncing).toBe(false);
  });

  it('keeps syncing when autoRefreshUntilDone and loadState succeeds', () => {
    component.syncing = true;
    component.autoRefreshUntilDone = true;
    spyOn(syncService, 'getState').and.returnValue(of({ integrations: [] }));

    component.loadState(
      () => {
        if (!component.autoRefreshUntilDone) {
          component.syncing = false;
          component.resyncing = false;
        }
      },
      () => {}
    );

    expect(component.syncing).toBe(true);
  });

  it('clears syncing and resyncing when loadState fails after sync trigger', () => {
    component.syncing = true;
    component.resyncing = true;
    spyOn(syncService, 'getState').and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.loadState(
      () => {},
      () => {
        component.syncing = false;
        component.resyncing = false;
      }
    );

    expect(component.syncing).toBe(false);
    expect(component.resyncing).toBe(false);
  });

  it('totalPendingCount sums pendingCount from integrations', () => {
    component.state = {
      integrations: [
        {
          syncKey: 'default',
          recordType: 'skill',
          syncWatermark: '2025-01-01',
          pendingCount: 5,
        },
        {
          syncKey: 'default',
          recordType: 'collection',
          syncWatermark: '2025-01-01',
          pendingCount: 2,
        },
      ],
    };
    expect(component.totalPendingCount).toBe(7);
    expect(component.pendingByRecordType).toEqual({
      skills: 5,
      collections: 2,
    });
  });
});
