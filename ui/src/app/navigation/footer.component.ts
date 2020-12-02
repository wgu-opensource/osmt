import { Component, OnInit } from "@angular/core"
import {Whitelabelled} from "../../whitelabel"

@Component({
  selector: "app-footer",
  templateUrl: "./footer.component.html"
})
export class FooterComponent extends Whitelabelled implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }



}
