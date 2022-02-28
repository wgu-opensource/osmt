import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {CollectionService} from "../../../../collection/service/collection.service"
import {ApiCollectionSearchResult} from "../../api/ApiCollectionSearchResult"
import {ApiCollectionSummary} from "../../../../richskill/ApiSkillSummary";

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-external-search-collection-row]",
  templateUrl: "./external-search-collection-row.component.html"
})
export class ExternalSearchCollectionRowComponent implements OnInit {

  @Input() searchResult?: ApiCollectionSearchResult
  @Input() id = "collection-list-row"
  @Input() nextId = ""

  imported = false

  externalLinkIcon = SvgHelper.path(SvgIcon.EXTERNAL_LINK)

  get collection(): ApiCollectionSummary | undefined {
    return this.searchResult?.collection
  }

  get importButtonLabel(): string {
    return (this.imported) ? "Collection Imported" : "Import Collection"
  }

  constructor(
    protected collectionService: CollectionService
  ) {}

  ngOnInit(): void {
    if (!this.id) {
      throw Error()
    }
  }

  onImportCollectionClicked(): void {
    if (this.collection) {
      this.collectionService.importCollection(
        this.collection.id,
        this.collection.name
      ).subscribe(resp => {
        this.imported = true
      })
    }
  }
}
