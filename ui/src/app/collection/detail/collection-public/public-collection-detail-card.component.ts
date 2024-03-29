import {Component, Input, OnInit} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {ToastService} from "../../../toast/toast.service"
import {ApiCollection} from "../../ApiCollection"
import {PublishStatus} from "../../../PublishStatus"

@Component({
  selector: "app-collection-public-detail-card",
  templateUrl: "./public-collection-detail-card.component.html"
})
export class PublicCollectionDetailCardComponent implements OnInit {

  @Input() collection: ApiCollection | null = null
  @Input() totalSkills = 0
  @Input() indexOfFirstSkill = 0
  @Input() currentOnPage = 0
  @Input() showSkillCount = true

  constructor(protected router: Router,
              protected route: ActivatedRoute,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService
  ) {
  }

  get indexOfLastSkill(): number {
    return this.indexOfFirstSkill + this.currentOnPage
  }

  ngOnInit(): void {
  }

  get status(): string | undefined {
    return this.collection?.status
  }

  get archiveDate(): Date | undefined {
    return this.collection?.archiveDate
  }

  get publishDate(): Date | undefined {
    return this.collection?.publishDate
  }

  get collectionName(): string {
    return this.collection?.name ?? ""
  }

  get collectionDescription(): string {
    return this.collection?.description ?? ""
  }

  get collectionAuthor(): string {
    return this.collection?.author ?? ""
  }

  get displayStatus(): boolean {
    return this.collection?.status !== PublishStatus.Workspace
  }

  get displayLabel(): boolean {
    return this.collection?.status !== PublishStatus.Workspace
  }
}
