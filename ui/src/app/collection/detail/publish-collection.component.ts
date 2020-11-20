import {Component, OnInit} from "@angular/core";
import {Location} from "@angular/common";
import {ApiCollection} from "../ApiCollection";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {CollectionService} from "../service/collection.service";
import {PublishStatus} from "../../PublishStatus";
import {Observable} from "rxjs";
import {PaginatedSkills} from "../../richskill/service/rich-skill-search.service";

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

  checkingArchived = true
  get checkingDraft(): boolean { return !this.checkingArchived }

  batchSize = 50

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

    this.checkingArchived = true
    this.checkForStatus(PublishStatus.Archived)
  }

  get verb(): string {
    return (this.checkingArchived) ? "archived" : "unpublished"
  }

  checkForStatus(status: PublishStatus): void {
    this.skillsLoaded = this.collectionService.getCollectionSkills(this.uuidParam!, this.batchSize, 0, new Set([status]))
    this.skillsLoaded.subscribe(results => {
      this.blockingSkills = results
      if (results.totalCount < 1 && this.checkingArchived) {
        this.checkingArchived = false
        this.checkForStatus(PublishStatus.Unarchived)
      }
    })
  }

  handleClickCancel(): boolean {
    this.location.back()
    return false
  }

  handleClickConfirmRemove(): boolean {
    return false
  }

  handleClickConfirmUnarchive(): boolean {
    return false
  }

  handleClickConfirmPublish(): boolean {
    return false
  }
}
