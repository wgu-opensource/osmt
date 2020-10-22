import { Component, OnInit } from "@angular/core"
import {AbstractSearchComponent} from "./abstract-search.component";

@Component({
  selector: "app-commoncontrols-mobile",
  templateUrl: "./commoncontrols-mobile.component.html"
})
export class CommoncontrolsMobileComponent extends AbstractSearchComponent implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }

}
