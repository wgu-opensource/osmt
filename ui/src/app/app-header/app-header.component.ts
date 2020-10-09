import { Component, OnInit } from "@angular/core"
import {Whitelabelled} from "../../whitelabel";

@Component({
  selector: "app-app-header",
  templateUrl: "./app-header.component.html",
})
export class AppHeaderComponent extends Whitelabelled implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }

}
