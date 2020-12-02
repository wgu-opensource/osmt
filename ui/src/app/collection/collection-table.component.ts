import {Component, Input} from "@angular/core";
import {ICollectionSummary} from "../richskill/ApiSkillSummary";
import {AbstractTableComponent} from "../table/abstract-table.component";

@Component({
  selector: "app-collection-table",
  templateUrl: "./collection-table.component.html"
})
export class CollectionTableComponent extends AbstractTableComponent<ICollectionSummary> {

  @Input() allowSorting = false

}
