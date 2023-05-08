import { Component, OnInit } from "@angular/core"
import {Whitelabelled} from "../../whitelabel"

@Component({
  selector: "app-footer",
  templateUrl: "./footer.component.html",
  styleUrls: ["header.component.scss"]

})
export class FooterComponent extends Whitelabelled implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }



}
