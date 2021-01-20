import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {ToastService} from "../toast/toast.service";
import {PaginatedCollections} from "../richskill/service/rich-skill-search.service";
import {CollectionService} from "../collection/service/collection.service";
import {CollectionsListComponent} from "../collection/collections-list.component";
import {determineFilters} from "../PublishStatus";
import {Title} from "@angular/platform-browser";

@Component({
  selector: "app-collections-library",
  templateUrl: "../collection/collections-list.component.html"
})
export class CollectionsLibraryComponent extends CollectionsListComponent implements OnInit {
  title = "Collections"

  constructor(
    protected router: Router,
    protected toastService: ToastService,
    protected collectionService: CollectionService,
    protected titleService: Title
  ) {
    super(router, toastService, collectionService)
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Collections | ${this.whitelabel.toolName}`)
    this.loadNextPage()
  }

  loadNextPage(): void {
    if (this.selectedFilters.size < 1) {
      this.setResults(new PaginatedCollections([], 0))
      return
    }

    this.resultsLoaded = this.collectionService.getCollections(this.size, this.from, determineFilters(this.selectedFilters), this.columnSort)
    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

  getSelectAllEnabled(): boolean {
    return false
  }
}
