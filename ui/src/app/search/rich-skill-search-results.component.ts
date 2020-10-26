import {Component, OnInit} from "@angular/core";
import {SearchService} from "./search.service";
import {RichSkillService} from "../richskill/service/rich-skill.service";
import {Observable} from "rxjs";
import {ApiSkill} from "../richskill/ApiSkill";
import {ApiSearch} from "../richskill/service/rich-skill-search.service";


@Component({
  selector: "app-rich-skill-search-results",
  templateUrl: "./rich-skill-search-results.component.html"
})
export class RichSkillSearchResultsComponent implements OnInit {
  resultsLoaded: Observable<ApiSkill[]> | undefined

  private results: ApiSkill[] | undefined

  constructor(private searchService: SearchService, private richSkillService: RichSkillService) {
    searchService.searchQuery$.subscribe(apiSearch => this.handleNewSearch(apiSearch) )
  }

  ngOnInit(): void {
  }

  private handleNewSearch(apiSearch: ApiSearch): void {
    this.resultsLoaded = this.richSkillService.searchSkills(apiSearch)
    this.resultsLoaded.subscribe(results => this.setResults(results))
  }

  private setResults(results: ApiSkill[]): void {
    this.results = results
  }
}
