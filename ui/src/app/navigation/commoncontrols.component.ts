import { Component, OnInit } from "@angular/core"
import {FormControl, FormGroup} from "@angular/forms";
import {AbstractSearchComponent} from "./abstract-search.component";

@Component({
  selector: "app-commoncontrols",
  templateUrl: "./commoncontrols.component.html"
})
export class CommoncontrolsComponent extends AbstractSearchComponent implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }

}
