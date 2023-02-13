import { Component, OnInit } from "@angular/core"
import { KeywordType } from "../../richskill/ApiSkill"

@Component({
  selector: "app-filter",
  templateUrl: "./filter.component.html",
  styleUrls: ["./filter.component.scss"]
})
export class FilterComponent implements OnInit {

  keywordType = KeywordType

  constructor() {
  }

  ngOnInit(): void {
  }

}
