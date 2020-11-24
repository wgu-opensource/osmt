import { Component, Input, OnInit } from "@angular/core"
import { IDetailCardSectionData } from "./section/section.component"
import {PublishStatus} from "../PublishStatus";

@Component({
  selector: "app-detail-card",
  templateUrl: "./detail-card.component.html"
})
export class DetailCardComponent implements OnInit {

  @Input() sections: IDetailCardSectionData[] = []
  @Input() title = ""
  @Input() titleLabel = ""
  @Input() author = ""
  @Input() publishDate = ""
  @Input() archiveDate = ""
  @Input() status = PublishStatus.Draft

  constructor() { }

  ngOnInit(): void {
  }

  cardTypeLabel(): string {
    return "RichSkillDescriptor"
  }
}
