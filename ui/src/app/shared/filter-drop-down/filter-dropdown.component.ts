import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import { KeywordType } from "../../richskill/ApiSkill"
import { FormBuilder, FormGroup } from "@angular/forms"
import {FilterDropdown} from "../../models/filter-dropdown.model"

@Component({
  selector: "app-filter-dropdown",
  templateUrl: "./filter-dropdown.component.html",
  styleUrls: ["./filter-dropdown.component.scss"]
})
export class FilterDropdownComponent implements OnInit {

  @Output()
  applyFilter = new EventEmitter<FilterDropdown>()
  showInputs = false
  filterFg: FormGroup

  keywordType = KeywordType
  @Input()
  chipsValues?: FilterDropdown

  constructor(private formBuilder: FormBuilder) {
    this.filterFg = this.configureFilterFg()
  }

  ngOnInit(): void {
    this.filterFg.patchValue(this.chipsValues ?? {})
  }

  private configureFilterFg(): FormGroup {
    return this.formBuilder.group({
      categories: [],
      keywords: [],
      standards: [],
      alignment: [],
      certifications: [],
      occupations: [],
      employers: []
    })
  }

  onApplyFilter(): void {
    this.showInputs = !this.showInputs
    this.applyFilter.emit(this.filterFg.value)
  }

}
