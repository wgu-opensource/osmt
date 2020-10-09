import { Component, OnInit } from "@angular/core"

@Component({
  selector: "app-action-bar",
  templateUrl: "./action-bar.component.html"
})
export class ActionBarComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  onCopyURL(): void {
    console.log("You just clicked Copy URL")
  }

  onDownloadCsv(): void {
    console.log("You just clicked Download CSV")
  }

  onCopyJSON(): void {
    console.log("You just clicked Copy JSON")
  }

}
