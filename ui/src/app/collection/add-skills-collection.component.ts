import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {ApiSkillSummary} from "../richskill/ApiSkillSummary";
import {Title} from "@angular/platform-browser";
import {PublishStatus} from "../PublishStatus";

@Component({
  selector: "app-add-skills-collection",
  templateUrl: "./add-skills-collection.component.html"
})
export class AddSkillsCollectionComponent implements OnInit {
  selectedSkills?: ApiSkillSummary[]

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])

  get isPlural(): boolean {
    return (this.selectedSkills?.length ?? 0) > 1
  }

  constructor(protected router: Router, protected titleService: Title) {
    this.selectedSkills = this.router.getCurrentNavigation()?.extras.state as ApiSkillSummary[]
    this.titleService.setTitle("Add RSDs to a Collection")
  }

  ngOnInit(): void {
    if ((this.selectedSkills?.length ?? 0) < 1) {
      this.router.navigate(["/"])
    }
  }

  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
  }
}
