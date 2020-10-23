import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSkill} from "../richskill/ApiSkill"
import {Observable} from "rxjs"
import {SkillWithSelection} from "./table-row/table-row.component"

/**
 * Implement row components to hold datasets and figure out how to dynamically pass and use them
 */
@Component({
  selector: "app-table",
  templateUrl: "./table.component.html"
})
export class TableComponent implements OnInit {

  @Input() skills: Observable<ApiSkill[]> | null = null

  @Output() columnSortEmitter = new EventEmitter<string>()

  private selectAllTemplate = "Select All (%s)"
  tableColumns: string[] = ["Category", "Skill", "Select All ()"]

  // can I change this type?  Just pass along the same observable to all components that need it.
  preparedSkills: SkillWithSelection[] = [] // skillwithselection not necessary, the presence of a skill in an event is proof

  @Output() selectedRowEmitter: EventEmitter<ApiSkill[]> = new EventEmitter<ApiSkill[]>()

  constructor() { }

  ngOnInit(): void {
    this.skills?.subscribe(skills => {
      this.preparedSkills = skills.map<SkillWithSelection>(skill => ({skill, selected: false}))
    })
  }

  numberOfSelected(): number {
    return this.preparedSkills.filter(pk => pk.selected).length
  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle(skill: ApiSkill): void {
    const maybeFound = this.preparedSkills.find(s => s.skill === skill)
    if (maybeFound) {
      const newStatus = !maybeFound.selected
      maybeFound.selected = newStatus
    }
  }

  // propogate the header component's column sort event upward
  headerColumnSortPerformed(columnName: string): void {
    this.columnSortEmitter.emit(columnName)
  }

  selectAll(selected: boolean): void {
    this.preparedSkills.map(skill => skill.selected = selected)
  }

}
