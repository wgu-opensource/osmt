import {Component, OnInit} from "@angular/core";
import {Location} from "@angular/common";
import {ApiCollection, ApiCollectionUpdate} from "../ApiCollection";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {CollectionService} from "../service/collection.service";
import {PublishStatus} from "../../PublishStatus";
import {Observable} from "rxjs";
import {ApiSearch, ApiSkillListUpdate, PaginatedSkills} from "../../richskill/service/rich-skill-search.service";
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
    this.activeState += 1
    if (this.activeState === PubColState.checkingArchived) {
      this.checkForStatus(new Set([PublishStatus.Archived, PublishStatus.Deleted]))
    }
    else if (this.activeState === PubColState.checkingDraft) {
      this.checkForStatus(new Set([PublishStatus.Draft]))
    }
    else if (this.activeState === PubColState.publishingCollection) {
      this.submitCollectionStatusChange()
    }
    else if (this.activeState === PubColState.end) {
      this.toastService.hideBlockingLoader()
    }
  }

  get verb(): string {
    return (this.activeState === PubColState.checkingArchived) ? "archived" : "unpublished"
  }

  checkForStatus(statuses: Set<PublishStatus>): void {

    this.skillsLoaded = this.collectionService.getCollectionSkills(this.uuidParam!, this.batchSize, 0, statuses)
    this.skillsLoaded.subscribe(results => {
      this.blockingSkills = results
      if (results.totalCount < 1) {
        this.nextState()
      } else {
        this.toastService.hideBlockingLoader()
      }
    })
  }

  handleClickCancel(): boolean {
    this.location.back()
    return false
  }

  handleClickConfirmRemove(): boolean {
    // remove all archived or deleted skills from collection
    const skillListUpdate = new ApiSkillListUpdate({
      remove: new ApiSearch({})
    })
    const filterBy = new Set([PublishStatus.Archived, PublishStatus.Deleted])
    this.toastService.showBlockingLoader()
    this.skillsSaved = this.collectionService.updateSkillsWithResult(this.uuidParam!, skillListUpdate, filterBy)
    this.skillsSaved.subscribe(result => {
      if (result) {
        this.nextState()
      }
    })
    return false
  }

  handleClickConfirmUnarchive(): boolean {
    // unarchive all archived or deleted skills from collection
    this.submitSkillStatusChange(PublishStatus.Unarchived, new Set([PublishStatus.Archived, PublishStatus.Deleted]))
    return false
  }

  handleClickConfirmPublish(): boolean {
    // publish all draft skills
    this.submitSkillStatusChange(PublishStatus.Published, new Set([PublishStatus.Draft]))
    return false
  }

  submitSkillStatusChange(newStatus: PublishStatus, filterStatuses: Set<PublishStatus>): void {
    const search = new ApiSearch({})
    this.toastService.showBlockingLoader()
    this.skillsSaved = this.richSkillService.publishSkillsWithResult(search, newStatus, filterStatuses, this.uuidParam)
    this.skillsSaved.subscribe(result => {
      if (!result) { return }
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
