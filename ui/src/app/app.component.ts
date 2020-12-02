import {Component, OnInit} from "@angular/core"
import {Title} from "@angular/platform-browser"
import {Whitelabelled} from "../whitelabel"

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.css"],
})
export class AppComponent extends Whitelabelled implements OnInit {
  public constructor(private titleService: Title) {
    super()
  }

  ngOnInit(): void {
    this.titleService.setTitle(this.whitelabel.toolName)
  }
}
