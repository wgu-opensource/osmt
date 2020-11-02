import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSkillSortOrder} from "../richskill/ApiSkill"
import {Observable} from "rxjs"
import {SkillWithMetadata} from "./table-row/table-row.component"
import {IApiSkillSummary} from "../richskill/ApiSkillSummary"
import {TableActionDefinition} from "./has-action-definitions";

/**
 * Implement row components to hold datasets and figure out how to dynamically pass and use them
 */
@Component({
  selector: "app-table",
  templateUrl: "./table.component.html"
})
export class TableComponent implements OnInit {

  @Input() skills: IApiSkillSummary[] = []
  @Input() currentlySortedColumn: ApiSkillSortOrder | undefined = undefined
  @Input() rowActions: TableActionDefinition[] = []

  // Any time a row is selected, broadcast out the list of currently selected skills from preparedSkills
  @Output() rowSelected: EventEmitter<IApiSkillSummary[]> = new EventEmitter<IApiSkillSummary[]>()
  @Output() columnSorted = new EventEmitter<ApiSkillSortOrder>()
  @Output() selectAllSelected = new EventEmitter<boolean>()

  // handles the inner state of the loaded skills
  preparedSkills: SkillWithMetadata[] = []

  constructor() { }

  ngOnInit(): void {
    this.preparedSkills = this.skills?.map<SkillWithMetadata>(skill => ({skill, selected: false})) ?? []
  }

  getSelectedSkills(): IApiSkillSummary[] {
    return this.preparedSkills.filter(row => row.selected).map(row => row.skill)
  }

  numberOfSelected(): number {
    return this.preparedSkills.filter(pk => pk.selected).length
  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle(skill: IApiSkillSummary): void {
    const maybeFound = this.preparedSkills.find(s => s.skill === skill)
    if (maybeFound) {
      maybeFound.selected = !maybeFound.selected
    }
    this.rowSelected.emit(this.getSelectedSkills())

  }

  // propagate the header component's column sort event upward
  headerColumnSortPerformed(columnName: ApiSkillSortOrder): void {
    this.columnSorted.emit(columnName)
  }

  handleSelectAll(selected: boolean): void {
    this.preparedSkills.forEach(skill => skill.selected = selected)
    this.selectAllSelected.emit(selected)
  }

}
