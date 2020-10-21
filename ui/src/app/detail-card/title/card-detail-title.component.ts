import {Component, Input, OnInit} from "@angular/core"

@Component({
  selector: "app-card-detail-title",
  templateUrl: "./card-detail-title.component.html"
})
export class CardDetailTitleComponent implements OnInit {

  @Input() cardType = ""
  @Input() title = ""
  @Input() author = ""
  @Input() published = ""
  @Input() archived = ""

  constructor() { }

  ngOnInit(): void {
  }

}
