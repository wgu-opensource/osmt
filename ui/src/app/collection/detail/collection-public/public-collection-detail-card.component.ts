import {Component, Input, OnInit} from "@angular/core"
import {SkillsListComponent} from "../../../richskill/list/skills-list.component"
import {ActivatedRoute, Router} from "@angular/router"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {ToastService} from "../../../toast/toast.service"
import {Collection} from "../../Collection"

@Component({
  selector: "app-collection-public-detail-card",
  templateUrl: "./public-collection-detail-card.component.html"
})
export class PublicCollectionDetailCardComponent implements OnInit {

  @Input() collection: Collection | null = null
  @Input() totalSkills = 0
  @Input() indexOfFirstSkill = 0
  @Input() currentOnPage = 0

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
}
