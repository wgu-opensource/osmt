import {Component, EventEmitter, OnInit, Output} from "@angular/core"
import { KeywordType } from "../../richskill/ApiSkill"
import { FormBuilder, FormGroup } from "@angular/forms"

@Component({
  selector: "app-filter-dropdown",
  templateUrl: "./filter-dropdown.component.html",
  styleUrls: ["./filter-dropdown.component.scss"]
})
export class FilterDropdownComponent implements OnInit {

  @Output()
  applyFilter = new EventEmitter<any>()
  showInputs = false
  filterFg: FormGroup

  keywordType = KeywordType
  categories: string[] = []
  keywords: string[] = []
  standards: string[] = []
  certifications: string[] = []
  occupations: string[] = []
  employers: string[] = []

  constructor(private formBuilder: FormBuilder) {
    this.filterFg = this.configureFilterFg()
  }

  ngOnInit(): void {
  }

  private configureFilterFg(): FormGroup {
    return this.formBuilder.group({
      categories: "",
      keywords: "",
      standards: "",
      certifications: "",
      occupations: "",
      employers: ""
    })
  }

  onApplyFilter(): void {
    this.showInputs = !this.showInputs
    this.applyFilter.emit({
      categories: this.categories,
      keywords: this.keywords,
      standards: this.standards,
      certifications: this.certifications,
      occupations: this.occupations,
      employers: this.employers
    })
  }

}
