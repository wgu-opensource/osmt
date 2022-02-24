import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiCollectionSummary, ICollectionSummary} from "../../../../richskill/ApiSkillSummary"
import {TableActionDefinition} from "../../../../table/skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-external-search-collection-row]",
  templateUrl: "./external-search-collection-row.component.html"
})
export class ExternalSearchCollectionRowComponent implements OnInit {

  @Input() collection?: ApiCollectionSummary
  @Input() id = "collection-list-row"
  @Input() nextId = ""

  externalLinkIcon = SvgHelper.path(SvgIcon.EXTERNAL_LINK)

  constructor() { }

  ngOnInit(): void {
    if (!this.id) {
      throw Error()
    }
  }

  onImportCollectionClicked(): void {
    // TODO: Call Import.
  }
}
