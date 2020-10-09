import { Component, OnInit } from "@angular/core"
import {Whitelabelled} from "../../whitelabel";

@Component({
  selector: "app-app-footer",
  templateUrl: "./app-footer.component.html"
})
export class AppFooterComponent extends Whitelabelled implements OnInit {

  constructor() {
    super();
  }

  ngOnInit(): void {
  }



}
