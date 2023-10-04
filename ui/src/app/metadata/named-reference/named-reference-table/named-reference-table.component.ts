import {AfterViewInit, Component, Input, QueryList, ViewChildren} from "@angular/core"
import { AbstractTableComponent } from "../../../table/abstract-table.component"
import { NamedReferenceListRowComponent } from "../named-reference-list-row/named-reference-list-row.component"
import { NamedReferenceInterface } from "../NamedReference"
import { MetadataType } from "../../rsd-metadata.enum";
import { ApiSortOrder } from "../../../richskill/ApiSkill";

@Component({
  selector: "app-named-reference-table",
  templateUrl: "./named-reference-table.component.html"
})
export class NamedReferenceTableComponent extends AbstractTableComponent<NamedReferenceInterface> implements AfterViewInit {

  @ViewChildren(NamedReferenceListRowComponent) rowReferences: QueryList<NamedReferenceListRowComponent> | undefined = undefined

  @Input() allowSorting = false
  @Input() metadataSelected? : MetadataType


  get isAlignmentSelected(): boolean {
    return this.metadataSelected == MetadataType.Alignment
  }

  ngAfterViewInit(): void {
    if (this.rowReferences && this.rowReferences.length > 0) {
      this.rowReferences.first.focusFirstColumnInRow()
    }
  }

  sortColumn(column: string, ascending: boolean): void {
    if (column.toLowerCase() === "name") {
      this.currentSort = ascending ? ApiSortOrder.KeywordNameAsc : this.currentSort = ApiSortOrder.KeywordNameDesc
    } else if (column.toLowerCase() === "framework") {
      this.currentSort = ascending ? ApiSortOrder.KeywordFrameworkAsc : ApiSortOrder.KeywordFrameworkDesc
    }
    this.columnSorted.emit(this.currentSort)
  }

  getFrameworkSort(): boolean | undefined {
    return this.currentSort == ApiSortOrder.KeywordFrameworkAsc;
  }

}
