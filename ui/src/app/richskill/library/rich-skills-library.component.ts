import {Component, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {SkillsListComponent} from "../../table/skills-list.component";
import {ToastService} from "../../toast/toast.service";

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "../../table/skills-list.component.html"
})
export class RichSkillsLibraryComponent extends SkillsListComponent implements OnInit {

  title = "Skills Library"

  constructor(
    protected richSkillService: RichSkillService,
    protected toastService: ToastService
  ) {
    super(richSkillService, toastService)
  }

  ngOnInit(): void {
    this.loadNextPage()
  }

  loadNextPage(): void {
    this.resultsLoaded = this.richSkillService.getSkills(this.size, this.from, this.selectedFilters, this.columnSort)
    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

}
