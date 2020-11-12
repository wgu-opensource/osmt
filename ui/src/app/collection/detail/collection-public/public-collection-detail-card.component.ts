import { Component, OnInit } from "@angular/core"
import {SkillsListComponent} from "../../../richskill/list/skills-list.component"
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../../../richskill/service/rich-skill.service";
import {ToastService} from "../../../toast/toast.service";

@Component({
  selector: "app-collection-public-detail-card",
  templateUrl: "./public-collection-detail-card.component.html"
})
export class PublicCollectionDetailCardComponent implements OnInit {

  uuidParam: string | null = ""

  constructor(protected router: Router,
              protected route: ActivatedRoute,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService
  ) {
  }

  ngOnInit(): void {
    this.uuidParam = this.route.snapshot.paramMap.get("uuid")
  }
}
