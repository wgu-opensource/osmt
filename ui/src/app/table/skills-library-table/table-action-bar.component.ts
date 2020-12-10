import {Component, ElementRef, OnInit, ViewChild} from "@angular/core";
import {HasActionDefinitions} from "./has-action-definitions";


@Component({
  selector: "app-table-action-bar",
  templateUrl: "./table-action-bar.component.html"
})
export class TableActionBarComponent extends HasActionDefinitions implements OnInit {

  @ViewChild("collectionDetailTableActionBar") actionBar!: ElementRef

  constructor() {
    super()
  }

  ngOnInit(): void {
  }

  focus(): void {
    this.actionBar.nativeElement.focus()
  }
}
