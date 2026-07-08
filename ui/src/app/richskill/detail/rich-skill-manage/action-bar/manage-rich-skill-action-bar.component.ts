import { Router } from '@angular/router';
import { RichSkillService } from '../../../service/rich-skill.service';
import { ToastService } from '../../../../toast/toast.service';
import {
  Component,
  EventEmitter,
  Inject,
  LOCALE_ID,
  OnInit,
} from '@angular/core';
import { AppConfig } from '../../../../app.config';
import { openPublishedSkillBrowserUrl } from '../../../../core/canonical-public-url.utils';
import { SvgHelper, SvgIcon } from '../../../../core/SvgHelper';
import { PublishStatus } from '../../../../PublishStatus';
import { ExtrasSelectedSkillsState } from '../../../../collection/add-skills-collection.component';
import { ApiSkillSummary } from '../../../ApiSkillSummary';
import { AuthService } from '../../../../auth/auth-service';
import { ButtonAction } from '../../../../auth/auth-roles';
import { SyncService } from '../../../../admin/sync/sync.service';
import { CollectionService } from '../../../../collection/service/collection.service';
import {
  ApiSearch,
  ApiSkillListUpdate,
} from '../../../service/rich-skill-search.service';

@Component({ template: '' })
export abstract class ManageRichSkillActionBarComponent implements OnInit {
  abstract skillUuid: string;
  abstract skillName: string;
  abstract skillPublicUrl: string;
  abstract archived: string | undefined;
  abstract published: string | undefined;
  abstract credentialEngineUrl: string | undefined;

  abstract reloadSkill: EventEmitter<void>;

  // Used in invisible labels to house the data to be added to clipboard
  abstract href: string;
  abstract jsonClipboard: string;

  // icons
  editIcon: string = SvgHelper.path(SvgIcon.EDIT);
  duplicateIcon: string = SvgHelper.path(SvgIcon.DUPLICATE);
  publishIcon: string = SvgHelper.path(SvgIcon.PUBLISH);
  externalLinkIcon: string = SvgHelper.path(SvgIcon.EXTERNAL_LINK);
  credentialEngineSyncIcon: string = SvgHelper.path(SvgIcon.UPLOAD);
  archiveIcon: string = SvgHelper.path(SvgIcon.ARCHIVE);
  dismissIcon: string = SvgHelper.path(SvgIcon.DISMISS);
  addToWorkspaceIcon: string = SvgHelper.path(SvgIcon.ADD);

  canSkillUpdate = false;
  canSkillCreate = false;
  canSkillPublish = false;
  canCollectionUpdate = false;
  canCollectionCreate = false;
  canCollectionPublish = false;
  canCollectionSkillsUpdate = false;
  isDraftAndDisabled = false;
  canSyncRecord = false;
  canAddToWorkspace = false;

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string,
    private authService: AuthService,
    protected syncService: SyncService,
    protected collectionService: CollectionService
  ) {}

  ngOnInit(): void {
    this.href = `${AppConfig.settings.baseApiUrl}${this.router.url}`;
    this.richSkillService
      .getSkillJsonByUuid(this.skillUuid)
      .subscribe((json: string) => {
        this.jsonClipboard = json;
      });
    this.setEnableFlags();
  }

  onAddToCollection(): void {
    const fakeSummary = new ApiSkillSummary({
      id: '',
      skillName: this.skillName,
      skillStatement: '',
      uuid: this.skillUuid,
      status: PublishStatus.Draft,
      categories: [],
      keywords: [],
      occupations: [],
    });
    this.router.navigate(['/collections/add-skills'], {
      state: {
        selectedSkills: [fakeSummary],
        totalCount: 1,
      } as ExtrasSelectedSkillsState,
    });
  }

  publishLinkText(): string {
    return this.isPublished() ? 'View Published RSD' : 'Publish';
  }

  publishLinkDestination(): string {
    return this.isPublished() ? '../' : ''; // TODO Implement
  }

  private isPublished(): boolean {
    return !!this.published;
  }

  handleArchive(): void {
    this.toastService.showBlockingLoader();
    this.richSkillService
      .updateSkill(this.skillUuid, {
        status: PublishStatus.Archived,
      })
      .subscribe(() => {
        this.reloadSkill.emit();
        this.toastService.hideBlockingLoader();
      });
  }

  handleUnarchive(): void {
    this.toastService.showBlockingLoader();
    this.richSkillService
      .updateSkill(this.skillUuid, {
        status: PublishStatus.Unarchived,
      })
      .subscribe(() => {
        this.reloadSkill.emit();
        this.toastService.hideBlockingLoader();
      });
  }

  handlePublish(): void {
    if (!this.published) {
      if (confirm('Are you sure you want to publish this RSD?')) {
        this.toastService.showBlockingLoader();
        this.richSkillService
          .updateSkill(this.skillUuid, {
            status: PublishStatus.Published,
          })
          .subscribe(() => {
            this.reloadSkill.emit();
            this.toastService.hideBlockingLoader();
          });
      }
    } else {
      window.open(
        openPublishedSkillBrowserUrl(this.skillUuid, this.skillPublicUrl),
        '_blank'
      );
    }
  }

  handleCopyPublicURL(): void {
    navigator.clipboard
      .writeText(this.skillPublicUrl)
      .then(() =>
        this.toastService.showToast('Success!', 'URL copied to clipboard')
      )
      .catch(() =>
        this.toastService.showToast('Error', 'Could not copy to clipboard')
      );
  }

  handleAddToWorkspace(): void {
    this.toastService.showBlockingLoader();
    this.collectionService.getWorkspace().subscribe({
      next: workspace => {
        const skillListUpdate = new ApiSkillListUpdate({
          add: new ApiSearch({ uuids: [this.skillUuid] }),
        });
        this.collectionService
          .updateSkillsWithResult(workspace.uuid, skillListUpdate)
          .subscribe({
            next: result => {
              if (result) {
                const message = `You added ${result.modifiedCount} RSDs to the workspace.`;
                this.toastService.showToast('Success!', message);
              }
              this.toastService.hideBlockingLoader();
            },
            error: () => this.toastService.hideBlockingLoader(),
          });
      },
      error: () => this.toastService.hideBlockingLoader(),
    });
  }

  setEnableFlags(): void {
    this.canSkillUpdate = this.authService.isEnabledByRoles(
      ButtonAction.SkillUpdate
    );
    this.canSkillCreate = this.authService.isEnabledByRoles(
      ButtonAction.SkillCreate
    );
    this.canSkillPublish = this.authService.isEnabledByRoles(
      ButtonAction.SkillPublish
    );
    this.canCollectionUpdate = this.authService.isEnabledByRoles(
      ButtonAction.CollectionUpdate
    );
    this.canCollectionCreate = this.authService.isEnabledByRoles(
      ButtonAction.CollectionCreate
    );
    this.canCollectionPublish = this.authService.isEnabledByRoles(
      ButtonAction.CollectionPublish
    );
    this.canCollectionSkillsUpdate = this.authService.isEnabledByRoles(
      ButtonAction.CollectionSkillsUpdate
    );
    this.canSyncRecord = this.authService.isEnabledByRoles(
      ButtonAction.SyncRecord
    );
    this.canAddToWorkspace = this.authService.isEnabledByRoles(
      ButtonAction.MyWorkspace
    );

    this.isDraftAndDisabled = !this.isPublished() && !this.canSkillPublish;
  }

  handleSyncToCredentialEngine(): void {
    this.toastService.showBlockingLoader();
    this.syncService.syncSkill(this.skillUuid).subscribe({
      next: () => {
        this.toastService.hideBlockingLoader();
        this.toastService.showToast(
          'Success',
          'Skill synced to the Credential Registry'
        );
        this.reloadSkill.emit();
      },
      error: err => {
        this.toastService.hideBlockingLoader();
        const msg =
          err?.status === 503
            ? 'Credential Registry sync is not configured'
            : (err?.error?.message ?? err?.message ?? 'Sync failed');
        this.toastService.showToast('Error', msg);
      },
    });
  }
}
