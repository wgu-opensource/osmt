import {
  Component,
  EventEmitter,
  Inject,
  Input,
  LOCALE_ID,
  Output,
} from '@angular/core';
import { Router } from '@angular/router';
import { RichSkillService } from '../../../../service/rich-skill.service';
import { ToastService } from '../../../../../toast/toast.service';
import { ManageRichSkillActionBarComponent } from '../manage-rich-skill-action-bar.component';
import { AuthService } from '../../../../../auth/auth-service';
import { SyncService } from '../../../../../admin/sync/sync.service';
import { CollectionService } from '../../../../../collection/service/collection.service';

@Component({
  selector: 'app-manage-skill-action-bar-vertical',
  templateUrl: './manage-skill-action-bar-vertical.component.html',
})
export class ManageSkillActionBarVerticalComponent extends ManageRichSkillActionBarComponent {
  @Input() skillUuid = '';
  @Input() skillName = '';
  @Input() skillPublicUrl = '';
  @Input() archived = undefined;
  @Input() published = undefined;
  @Input() credentialEngineUrl: string | undefined = undefined;

  @Output() reloadSkill = new EventEmitter<void>();

  href = '';
  jsonClipboard = '';

  constructor(
    router: Router,
    richSkillService: RichSkillService,
    toastService: ToastService,
    @Inject(LOCALE_ID) locale: string,
    authService: AuthService,
    syncService: SyncService,
    collectionService: CollectionService
  ) {
    super(
      router,
      richSkillService,
      toastService,
      locale,
      authService,
      syncService,
      collectionService
    );
  }
}
