import {Component, Input, OnInit} from "@angular/core"
import {PublishStatus} from "../../PublishStatus";

@Component({
  selector: "app-card-detail-title",
  templateUrl: "./card-detail-title.component.html"
})
export class CardDetailTitleComponent implements OnInit {

  @Input() cardType = ""
  @Input() title = ""
  @Input() author = ""
  @Input() status: PublishStatus = PublishStatus.Draft
  @Input() publishDate = ""
  @Input() archiveDate = ""

  constructor() { }

  ngOnInit(): void {
  }

}
