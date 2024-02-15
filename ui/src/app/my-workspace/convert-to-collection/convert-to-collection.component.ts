import {Component, OnInit} from "@angular/core"
import {CollectionFormComponent} from "../../collection/create-collection/collection-form.component"
import {CollectionService} from "../../collection/service/collection.service"
import {Location} from "@angular/common"
import {ActivatedRoute, Router} from "@angular/router"
import {ToastService} from "../../toast/toast.service"
import {Title} from "@angular/platform-browser"
import {ICollectionUpdate} from "../../collection/ApiCollection"
import {WORKSPACE_COLLECTIONS_UUIDS} from "../my-workspace.component"

@Component({
  selector: "app-convert-to-collection",
  templateUrl: "../../collection/create-collection/collection-form.component.html",
})
export class ConvertToCollectionComponent extends CollectionFormComponent implements OnInit {

  constructor(
    protected collectionService: CollectionService,
    protected loc: Location,
    protected router: Router,
    protected route: ActivatedRoute,
    protected toastService: ToastService,
    protected titleService: Title
  ) {
    super(collectionService, loc, router, route, toastService, titleService)
  }

  updateObject(): ICollectionUpdate {
    const formValues = this.collectionForm.value
    const collectionsUuids = localStorage.getItem(WORKSPACE_COLLECTIONS_UUIDS)
    return {
      name: formValues.collectionName,
      author: formValues.author,
      skills: {add: JSON.parse(collectionsUuids ?? "")}
    }
  }


  onSubmit(): void {
    const updateObject = this.updateObject()
    this.collectionService.createCollection(updateObject).subscribe(collection => {
        this.router.navigate([`/collections/${collection.uuid}/manage`]).then(() => {
          localStorage.removeItem(WORKSPACE_COLLECTIONS_UUIDS)
        })
      })
  }

}
