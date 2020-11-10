import {Component, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {SkillsListComponent} from "../list/skills-list.component";
import {ToastService} from "../../toast/toast.service";
import {PaginatedSkills} from "../service/rich-skill-search.service";
import {Router} from "@angular/router";

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "../list/skills-list.component.html"
})
export class RichSkillsLibraryComponent extends SkillsListComponent implements OnInit {

  title = "RSD Library"

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService
  ) {
    super(router, richSkillService, toastService)
  }

  ngOnInit(): void {
    this.loadNextPage()
  }

  loadNextPage(): void {
    if (this.selectedFilters.size < 1) {
      this.setResults(new PaginatedSkills([], 0))
      return
    }

    this.resultsLoaded = this.richSkillService.getSkills(this.size, this.from, this.selectedFilters, this.columnSort)
    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

  getSelectAllEnabled(): boolean {
    return false
  }
}
