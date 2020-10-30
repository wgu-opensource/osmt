import {Component, OnInit} from "@angular/core";
import {SearchService} from "./search.service";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {ApiSearch} from "../richskill/service/rich-skill-search.service";
import {ActivatedRoute} from "@angular/router";
import {ToastService} from "../toast/toast.service";
import {SkillsListComponent} from "../table/skills-list.component";


@Component({
  selector: "app-rich-skill-search-results",
  templateUrl: "../table/skills-list.component.html"
})
export class RichSkillSearchResultsComponent extends SkillsListComponent implements OnInit {

  apiSearch: ApiSearch | undefined

  title = "Search Results"

  constructor(protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected searchService: SearchService,
              protected route: ActivatedRoute,
) {
    super(richSkillService, toastService)
    searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const query = params.q
      if (query && query.length > 0) {
        this.handleNewSearch(ApiSearch.factory({query}))
      }
    })
  }

  private handleNewSearch(apiSearch: ApiSearch): void {
    this.apiSearch = apiSearch
    this.matchingQuery = this.apiSearch.query
    this.loadNextPage()
  }

  loadNextPage(): void {
    if (this.apiSearch !== undefined) {
      this.resultsLoaded = this.richSkillService.searchSkills(this.apiSearch, this.size, this.from, this.selectedFilters, this.columnSort)
      this.resultsLoaded.subscribe(results => this.setResults(results))
    }
  }


}
