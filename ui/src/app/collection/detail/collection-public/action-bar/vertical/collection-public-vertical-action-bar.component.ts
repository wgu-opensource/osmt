import {Component, Inject, LOCALE_ID} from "@angular/core"
import {CollectionPublicActionBarComponent} from "../collection-public-action-bar.component"
import {CollectionService} from "../../../../service/collection.service"
import {Router} from "@angular/router"
import {ToastService} from "../../../../../toast/toast.service"

@Component({
  selector: "app-collection-public-vertical-action-bar",
  templateUrl: "./collection-public-vertical-action-bar.component.html"
})
export class CollectionPublicVerticalActionBarComponent extends CollectionPublicActionBarComponent {

  constructor(
    router: Router,
    collectionService: CollectionService,
    toastService: ToastService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(router, collectionService, toastService, locale)
  }
}
