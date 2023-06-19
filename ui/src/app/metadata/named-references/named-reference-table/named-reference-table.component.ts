import {AfterViewInit, Component, Input, QueryList, ViewChildren} from "@angular/core"
import {AbstractTableComponent} from "../../../table/abstract-table.component"
import {NamedReferenceListRowComponent} from "../named-reference-list-row/named-reference-list-row.component"
import {INamedReference} from "../NamedReference"
import {MetadataType} from "../../rsd-metadata.enum";

@Component({
  selector: "app-named-reference-table",
  templateUrl: "./named-reference-table.component.html"
})
export class NamedReferenceTableComponent extends AbstractTableComponent<INamedReference> implements AfterViewInit {

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
}
