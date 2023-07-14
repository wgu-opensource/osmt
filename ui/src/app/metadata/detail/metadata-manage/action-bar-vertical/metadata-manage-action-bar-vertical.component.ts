import {
  Component,
  EventEmitter,
  Inject,
  Input,
  LOCALE_ID,
  OnInit,
  Output } from "@angular/core";
import { Router} from "@angular/router";

import { AbstractDataService } from "src/app/data/abstract-data.service";
import { AppConfig } from "../../../../app.config";
import { ButtonAction } from "../../../../auth/auth-roles";
import { AuthService } from "../../../../auth/auth-service";
import { SvgHelper, SvgIcon } from "../../../../core/SvgHelper";
import { ToastService } from "../../../../toast/toast.service";


@Component({
  selector: "app-manage-metadata-action-bar-vertical",
  templateUrl: "./metadata-manage-action-bar-vertical.component.html"
})
export class ManageMetadataActionBarVerticalComponent implements OnInit {

  @Input() id: number = -1;
  @Input() metadataName: string = "";
  @Input() metadataPublicUrl: string = "";

  @Output() reloadMetadata: EventEmitter<void> = new EventEmitter<void>;

  // Used in invisible labels to house the data to be added to clipboard
  href: string = "";
  jsonClipboard: string = "";

  // icons
  editIcon: string = SvgHelper.path(SvgIcon.EDIT);
  duplicateIcon: string = SvgHelper.path(SvgIcon.DUPLICATE);

  canMetadataUpdate: boolean = false;
  canMetadataCreate: boolean = false;

  constructor(
    protected router: Router,
    protected metadataService: AbstractDataService,
    protected toastService: ToastService,
    private authService: AuthService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
  }

  ngOnInit(): void {
    this.href = `${AppConfig.settings.baseApiUrl}${this.router.url}`;
    // this.metadataService.getMetadataJsonById(this.id)
    //   .subscribe( (json: string) => {
    //     this.jsonClipboard = json
    //   });
    this.setEnableFlags();
  }

  handleCopyPublicURL(): void {
    navigator.clipboard.writeText(this.metadataPublicUrl)
      .then(
        () => this.toastService.showToast("Success!", "URL copied to clipboard")
      )
      .catch(
        () => this.toastService.showToast("Error", "Could not copy to clipboard")
      );
  }

  setEnableFlags(): void {
    this.canMetadataUpdate = this.authService.isEnabledByRoles(ButtonAction.MetadataUpdate);
    this.canMetadataCreate = this.authService.isEnabledByRoles(ButtonAction.MetadataCreate);
  }

}