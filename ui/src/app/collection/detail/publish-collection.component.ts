import {Component, OnInit} from "@angular/core";
import {Location} from "@angular/common";
import {ApiCollection, ApiCollectionUpdate} from "../ApiCollection";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {CollectionService} from "../service/collection.service";
import {PublishStatus} from "../../PublishStatus";
import {Observable} from "rxjs";
import {
  ApiAdvancedSearch,
  ApiSearch,
  ApiSkillListUpdate,
  PaginatedSkills
} from "../../richskill/service/rich-skill-search.service";
import {ApiBatchResult} from "../../richskill/ApiBatchResult";


enum PubColState {
  start,
  checkingArchived,
  checkingDraft,
  publishingCollection,
  end
}

@Component({
  selector: "app-publish-collection",
  templateUrl: "./publish-collection.component.html"
})
export class PublishCollectionComponent implements OnInit {
  uuidParam?: string
  collectionLoaded?: Observable<ApiCollection>
  collection?: ApiCollection

  skillsLoaded?: Observable<PaginatedSkills>
  blockingSkills?: PaginatedSkills

  skillsSaved?: Observable<ApiBatchResult>
  get checkingDraft(): boolean { return this.activeState === PubColState.checkingDraft }
  get checkingArchived(): boolean { return this.activeState === PubColState.checkingArchived }

  batchSize = 3

  activeState: PubColState = PubColState.start
  collectionSaved?: Observable<ApiCollection>

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected collectionService: CollectionService,
              protected route: ActivatedRoute,
              protected location: Location
  ) {
  }

  ngOnInit(): void {
    this.uuidParam = this.route.snapshot.paramMap.get("uuid") !
    this.collectionLoaded = this.collectionService.getCollectionByUUID(this.uuidParam)
    this.collectionLoaded.subscribe(collection => {
      this.collection = collection
    })

    this.activeState = PubColState.start
    this.nextState()
  }

  nextState(): void {
    console.log("nextstate", this.activeState, this.activeState + 1)
    this.activeState += 1
    if (this.activeState === PubColState.checkingArchived) {
      this.checkForStatus(PublishStatus.Archived)
    }
    else if (this.activeState === PubColState.checkingDraft) {
      this.checkForStatus(PublishStatus.Unarchived)
    }
    else if (this.activeState === PubColState.publishingCollection) {
      this.submitCollectionStatusChange()
    }
    else if (this.activeState === PubColState.end) {
      // done
      console.log("DONE")
      this.toastService.hideBlockingLoader()
    }
  }

  get verb(): string {
    return (this.activeState === PubColState.checkingArchived) ? "archived" : "unpublished"
  }

  checkForStatus(status: PublishStatus): void {
    this.skillsLoaded = this.collectionService.getCollectionSkills(this.uuidParam!, this.batchSize, 0, new Set([status]))
    this.skillsLoaded.subscribe(results => {
      this.blockingSkills = results
      if (results.totalCount < 1) {
        console.log("no archived skills!", status)
        this.nextState()
      } else {
        console.log("gotta deal with archived skills!", status)

      }
    })
  }

  handleClickCancel(): boolean {
    this.location.back()
    return false
  }

  handleClickConfirmRemove(): boolean {
    this.skillsSaved = this.collectionService.updateSkillsWithResult(this.uuidParam!, new ApiSkillListUpdate({
      remove: new ApiSearch({
        advanced: ApiAdvancedSearch.factory({
          status: PublishStatus.Archived
        })
      })
    }))
    this.skillsSaved.subscribe(result => {
      if (!result) { return }

      this.nextState()
    })
    return false
  }

  handleClickConfirmUnarchive(): boolean {
    this.submitSkillStatusChange(PublishStatus.Unarchived, PublishStatus.Archived)
    return false
  }

  handleClickConfirmPublish(): boolean {
    this.submitSkillStatusChange(PublishStatus.Published, PublishStatus.Unarchived)
    return false
  }

  submitSkillStatusChange(newStatus: PublishStatus, filterStatus: PublishStatus): void {
    const search = new ApiSearch({})
    this.toastService.showBlockingLoader()
    this.skillsSaved = this.richSkillService.publishSkillsWithResult(search, filterStatus, new Set([newStatus]), this.uuidParam)
    this.skillsSaved.subscribe(result => {
      if (!result) { return }
      console.log("status change complete", newStatus, result)
      this.nextState()
    })

  }

  submitCollectionStatusChange(): void {
    const updateObject = new ApiCollectionUpdate({status: PublishStatus.Published})
    this.toastService.showBlockingLoader()
    this.collectionSaved = this.collectionService.updateCollection(this.uuidParam!, updateObject)
    this.collectionSaved.subscribe(result => {
      this.toastService.hideBlockingLoader()
      this.toastService.showToast("Success!", "You published the collection.")
      this.location.back()
    })

  }

}
