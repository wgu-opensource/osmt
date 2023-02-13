import { Component, OnInit } from "@angular/core"
import { KeywordType } from "../../richskill/ApiSkill"
import { FormBuilder, FormGroup } from "@angular/forms"

@Component({
  selector: "app-filter",
  templateUrl: "./filter.component.html",
  styleUrls: ["./filter.component.scss"]
})
export class FilterComponent implements OnInit {

  showInputs = false
  filterFg: FormGroup

  keywordType = KeywordType

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
  }

}
