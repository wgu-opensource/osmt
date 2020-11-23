import {Component, Inject, LOCALE_ID} from "@angular/core"
import {CollectionPublicActionBarComponent} from "../collection-public-action-bar.component"
import {Router} from "@angular/router"
import {CollectionService} from "../../../../service/collection.service"
import {ToastService} from "../../../../../toast/toast.service"
import {TaskService} from "../../../../../task/task-service"

@Component({
  selector: "app-collection-public-horizontal-action-bar",
  templateUrl: "./collection-public-horizontal-action-bar.component.html"
})
export class CollectionPublicHorizontalActionBarComponent extends CollectionPublicActionBarComponent {

  constructor(
    router: Router,
    collectionService: CollectionService,
    toastService: ToastService,
    taskService: TaskService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(router, collectionService, toastService, taskService, locale)
  }

}
