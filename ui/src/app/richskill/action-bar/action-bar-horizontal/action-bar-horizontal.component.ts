import { Component, OnInit } from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../../app.config"

@Component({
  selector: "app-action-bar-horizontal",
  templateUrl: "./action-bar-horizontal.component.html"
})
export class ActionBarHorizontalComponent implements OnInit {

  constructor(private router: Router) { }

  // This gets mapped to a visually hidden textarea so that javascript has a node to copy from
  href = ""

  ngOnInit(): void {
    this.href = `${AppConfig.settings.baseApiUrl}${this.router.url}`
  }

  onCopyURL(fullPath: HTMLTextAreaElement): void {
    console.log(`copying ${fullPath.textContent}`)
    fullPath.select()
    document.execCommand("copy")
    fullPath.setSelectionRange(0, 0)
  }

  onDownloadCsv(): void {
    console.log("You just clicked Download CSV")
  }

  onCopyJSON(): void {
    console.log("You just clicked Copy JSON")
  }
}
