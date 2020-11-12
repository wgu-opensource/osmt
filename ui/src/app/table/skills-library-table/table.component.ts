import {Component, EventEmitter, Input, Output} from "@angular/core"
import {IApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../abstract-table.component"

@Component({
  selector: "app-table",
  templateUrl: "./table.component.html"
})
export class TableComponent extends AbstractTableComponent {

  @Output() selectAllSelected = new EventEmitter<boolean>()
  @Input() selectAllCount?: number

  constructor() {
    super()
  }

  getSelectedSkills(): IApiSkillSummary[] {
    return this.preparedSkills.filter(row => row.selected).map(row => row.skill)
  }

  numberOfSelected(): number {
    return this.preparedSkills.filter(pk => pk.selected).length
  }

  getSelectAllCount(): number {
    return (this.selectAllCount !== undefined) ? this.selectAllCount : this.preparedSkills.length
  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle(skill: IApiSkillSummary): void {
    const maybeFound = this.preparedSkills.find(s => s.skill === skill)
    if (maybeFound) {
      maybeFound.selected = !maybeFound.selected
    }
    this.rowSelected.emit(this.getSelectedSkills())
  }

  handleSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    const selected: boolean = checkbox.checked
    this.preparedSkills.forEach(skill => skill.selected = selected)
    this.selectAllSelected.emit(selected)
    this.rowSelected.emit(this.getSelectedSkills())
  }
}
