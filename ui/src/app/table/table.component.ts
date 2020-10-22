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

  preparedSkills: SkillWithSelection[] = []

  selectedRows: ApiSkill[] = []

  @Output() selectedRowEmitter: EventEmitter<ApiSkill[]> = new EventEmitter<ApiSkill[]>()

  constructor() { }

  ngOnInit(): void {
    this.skills?.subscribe(skills => {
      this.preparedSkills = skills.map<SkillWithSelection>(skill => ({skill, selected: false}))
    })
  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle({skill, selected}: SkillWithSelection): void {
    if (selected && !this.selectedRows.find(s => s === skill)) {
      this.selectedRows.push(skill)
    } else {
      this.selectedRows = this.selectedRows.filter(s => s !== skill)
    }
    console.log("\nselected rows: \n" + this.selectedRows.map(sk => `\t${sk.skillName}`).join("\n"))
    this.selectedRowEmitter.emit(this.selectedRows)
  }

}
