import {Component, OnInit} from "@angular/core";
import {HasActionDefinitions} from "./has-action-definitions";


@Component({
  selector: "app-table-action-bar",
  templateUrl: "./table-action-bar.component.html"
})
export class TableActionBarComponent extends HasActionDefinitions implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }

}
