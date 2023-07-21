import { Component, Input, OnInit } from "@angular/core";

import { IDetailCardSectionData } from "./section/section.component";

@Component({
  selector: "app-detail-card",
  templateUrl: "./detail-card.component.html"
})
export class DetailCardComponent implements OnInit {

  @Input() sections: IDetailCardSectionData[] = []
  @Input() title = ""
  @Input() titleLabel = ""
  @Input() authors = ""
  @Input() publishDate = ""
  @Input() archiveDate = ""
  @Input() status = ""

  constructor() {}

  ngOnInit(): void {}

  cardTypeLabel(): string {
    return "RichSkillDescriptor"
  }
}
