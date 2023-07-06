import {Component, Input, OnInit} from "@angular/core";


@Component({
  selector: "app-card-detail-title",
  templateUrl: "./card-detail-title.component.html"
})
export class CardDetailTitleComponent implements OnInit {

  @Input() cardType = ""
  @Input() title = ""
  @Input() authors = ""
  @Input() status = ""
  @Input() publishDate = ""
  @Input() archiveDate = ""

  constructor() {}

  ngOnInit(): void {
  }

}
