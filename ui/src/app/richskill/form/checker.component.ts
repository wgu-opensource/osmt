import {Component, Input, OnInit} from "@angular/core";
import {Observable} from "rxjs";
import {ApiSkillSummary} from "../ApiSkillSummary";

@Component({
  selector: "app-checker",
  templateUrl: "./checker.component.html"
})
export class CheckerComponent implements OnInit {

  @Input() searchingMessage = "Checking for skill statement similarity …"
  @Input() similarMessage = "RSDs contain skill statements that are very similar to those already in the library."
  @Input() affirmativeMessage = "No similar skill statements found in library."
  @Input() similarSkills?: ApiSkillSummary[]
  @Input() searching?: boolean


  ngOnInit(): void {
  }

  get isVisible(): boolean {
    return this.searching !== undefined
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
