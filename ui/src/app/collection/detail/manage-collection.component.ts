import {Component, OnInit} from "@angular/core"
import {ApiCollection} from "../ApiCollection";
import {ApiSearch} from "../../richskill/service/rich-skill-search.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CollectionService} from "../service/collection.service";
import {ToastService} from "../../toast/toast.service";
import {SkillsListComponent} from "../../richskill/list/skills-list.component";
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: "app-manage-collection",
  templateUrl: "./manage-collection.component.html"
})
export class ManageCollectionComponent extends SkillsListComponent implements OnInit {
  collection?: ApiCollection
  apiSearch?: ApiSearch

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  get isPlural(): boolean {
    return (this.results?.skills.length ?? 0) > 1
  }

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected collectionService: CollectionService,
              protected route: ActivatedRoute,
  ) {
    super(router, richSkillService, toastService)
  }

  ngOnInit(): void {
    const uuid = this.route.snapshot.paramMap.get("uuid") ?? ""
    this.collectionService.getCollectionByUUID(uuid).subscribe(collection => {
      this.collection = collection
      this.loadNextPage()
    })
  }

  loadNextPage(): void {
    if (this.collection === undefined) {
      return
    }

    this.resultsLoaded = this.collectionService.getCollectionSkills(
      this.collection.uuid,
      this.size,
      this.from,
      this.selectedFilters,
      this.columnSort,
      this.apiSearch
    )
    this.resultsLoaded.subscribe(results => this.setResults(results))
  }

  getSelectAllCount(): number {
    return this.totalCount
  }


  public get searchQuery(): string {
    return this.searchForm.get("search")?.value ?? ""
  }
  clearSearch(): boolean {
    this.searchForm.reset()
    this.apiSearch = undefined
    this.from = 0
    this.loadNextPage()
    return false
  }
  handleDefaultSubmit(): boolean {
    this.apiSearch = new ApiSearch({query: this.searchQuery})
    this.matchingQuery = [this.searchQuery]
    this.from = 0
    this.loadNextPage()
    return false
  }
}
