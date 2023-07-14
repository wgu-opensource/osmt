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
import { AuthService } from "../../../../auth/auth-service";
import { SvgHelper, SvgIcon } from "../../../../core/SvgHelper";
import { ToastService } from "../../../../toast/toast.service";


@Component({
  selector: "app-public-metadata-action-bar-vertical",
  templateUrl: "./metadata-public-action-bar-vertical.component.html"
})
export class PublicMetadataActionBarVerticalComponent implements OnInit {

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

}