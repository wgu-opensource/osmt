import { Component } from '@angular/core';
import { Title } from "@angular/platform-browser";
import { Location } from "@angular/common";
import { ActivatedRoute, Router } from "@angular/router";
import { CollectionFormComponent } from "../collection-form.component";
import { ExtrasSelectedSkillsState } from "../../add-skills-collection.component";
import { ApiSearch, ApiSkillListUpdate } from "../../../richskill/service/rich-skill-search.service";
import { CollectionService } from "../../service/collection.service";
import { ToastService } from "../../../toast/toast.service";

@Component({
  selector: 'app-batch-import-collection',
  templateUrl: "../collection-form.component.html"
})
export class BatchImportCollectionComponent extends CollectionFormComponent {

  state?: ExtrasSelectedSkillsState

  constructor(
    protected collectionService: CollectionService,
    protected loc: Location,
    protected router: Router,
    protected route: ActivatedRoute,
    protected toastService: ToastService,
    protected titleService: Title
  ) {
    super(collectionService, loc, router, route, toastService, titleService)
    this.state = this.router.getCurrentNavigation()?.extras.state as ExtrasSelectedSkillsState
  }

  onSubmit(): void {
    console.log(this.state?.selectedSkills?.length)
    const updateObject = this.updateObject()
    this.collectionSaved = this.collectionService.createCollection(updateObject)
    const update = new ApiSkillListUpdate({
      add: new ApiSearch({uuids: this.state?.selectedSkills?.map(it => it.uuid) })
    })

    if (this.collectionSaved) {
      this.collectionSaved.subscribe(collection => {
        this.collectionForm.markAsPristine()
        this.collectionService.updateSkillsWithResult(collection.uuid, update, undefined).subscribe(result => {
        if (result) {
          const message = `You added ${result.modifiedCount} RSDs to the collection.`
          this.toastService.showToast("Success!", message)
          this.toastService.hideBlockingLoader()
          this.router.navigate([`/collections/${collection.uuid}/manage`])
        }
        })
      })
    }
  }

}
