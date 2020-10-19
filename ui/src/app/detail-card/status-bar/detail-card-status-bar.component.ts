import {Component, Input, OnInit} from "@angular/core"

@Component({
  selector: "app-detail-card-status-bar",
  templateUrl: "./detail-card-status-bar.component.html"
})
export class DetailCardStatusBarComponent implements OnInit {

  @Input() published = ""
  @Input() archived = ""

  constructor() { }

  ngOnInit(): void {
  }

  isPublished(): boolean {
    return !!this.published
  }

  isArchived(): boolean {
    return !!this.archived
  }
}
