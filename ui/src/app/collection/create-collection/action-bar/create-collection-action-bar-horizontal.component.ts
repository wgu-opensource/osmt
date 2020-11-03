import {Component} from "@angular/core"
import {AbstractCreateCollectionActionbarComponent} from "./abstract-create-collection-actionbar.component"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

@Component({
  selector: "app-create-collection-action-bar-horizontal",
  template: `
  `
})
export class CreateCollectionActionBarHorizontalComponent extends AbstractCreateCollectionActionbarComponent {

  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)

  constructor() {
    super()
  }
}
