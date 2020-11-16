import {Component, OnInit} from "@angular/core"
import {ApiCollection} from "../ApiCollection";
import {ApiSearch} from "../../richskill/service/rich-skill-search.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CollectionService} from "../service/collection.service";
import {ToastService} from "../../toast/toast.service";
import {SkillsListComponent} from "../../richskill/list/skills-list.component";
import {RichSkillService} from "../../richskill/service/rich-skill.service";

@Component({
  selector: "app-manage-collection",
  templateUrl: "./manage-collection.component.html"
})
export class ManageCollectionComponent extends SkillsListComponent implements OnInit {
  collection?: ApiCollection
  apiSearch?: ApiSearch

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

}
