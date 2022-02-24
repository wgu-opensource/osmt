import {AfterViewInit, Component} from "@angular/core"
import {ICollectionSummary} from "../../../../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../../../../table/abstract-table.component"

@Component({
  selector: "app-external-search-collection-table",
  templateUrl: "./external-search-collection-table.component.html"
})
export class ExternalSearchCollectionTableComponent extends AbstractTableComponent<ICollectionSummary>{

  constructor() {
    super()
  }

}
