import {Component, Input, OnInit} from "@angular/core";
import {Observable} from "rxjs";
import {ApiSkillSummary} from "../ApiSkillSummary";

@Component({
  selector: "app-checker",
  templateUrl: "./checker.component.html"
})
export class CheckerComponent implements OnInit {

  @Input() searchingMessage = "Checking for skill statement similarity …"
  @Input() neutralMessage = "Similarity: —"
  @Input() similarMultipleMessage = "Skill statement is very similar to those in multiple RSDs."
  @Input() similarSingleMessage = "Skill statement is very similar to one already in the library."
  @Input() affirmativeMessage = "Skill statement OK."
  @Input() similarSkills?: ApiSkillSummary[]
  @Input() searching?: boolean

  @Input() showAccordion = true



  ngOnInit(): void {
  }

  get isNeutral(): boolean {
    return this.searching === undefined
  }

  get isSearching(): boolean {
    return this.searching ?? false
  }
  get isAffirmative(): boolean {
    return this.searching === false && this.similarSkills?.length === 0
  }
  get hasMatches(): boolean {
    return this.searching === false && (this.similarSkills?.length ?? 0) > 0
  }
  get hasSingleMatch(): boolean {
    return this.similarSkills?.length === 1
  }
}
