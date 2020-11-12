import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {ApiCollectionSummary} from "../richskill/ApiSkillSummary";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-collection-list-row]",
  templateUrl: "./collection-list-row.component.html"
})
export class CollectionListRowComponent implements OnInit {
  @Input() collection?: ApiCollectionSummary
  @Input() id = "collection-list-row"
  @Input() rowActions: TableActionDefinition[] = []

  constructor() { }

  ngOnInit(): void {
  }

}
