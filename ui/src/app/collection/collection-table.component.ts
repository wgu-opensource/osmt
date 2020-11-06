import {Component, Input, OnInit} from "@angular/core";
import {ApiCollectionSummary} from "../richskill/ApiSkillSummary";
import {TableActionDefinition} from "../table/has-action-definitions";
import {SvgHelper, SvgIcon} from "../core/SvgHelper";

@Component({
  selector: "app-collection-table",
  templateUrl: "./collection-table.component.html"
})
export class CollectionTableComponent implements OnInit {

  @Input() collections: ApiCollectionSummary[] = []
  @Input() rowActions: TableActionDefinition[] = []

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() {
  }

  ngOnInit(): void {
  }

  handleNewSelection(collection: ApiCollectionSummary): void {
    console.log("chose new collection", collection)
  }
}
