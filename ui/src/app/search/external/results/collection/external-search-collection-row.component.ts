import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {CollectionService} from "../../../../collection/service/collection.service"
import {ApiCollectionSearchResult} from "../../api/ApiCollectionSearchResult"
import {ApiCollectionSummary} from "../../../../richskill/ApiSkillSummary"
import {ToastService} from "../../../../toast/toast.service"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-external-search-collection-row]",
  templateUrl: "./external-search-collection-row.component.html"
})
export class ExternalSearchCollectionRowComponent implements OnInit {

  static IMPORT_SUCCESS_TITLE = "Success!"
  static IMPORT_SUCCESS_MSG = "You added this collection to the library."
  static IMPORT_CONFIRMATION_MSG =
    "Are you sure you want to import this collection? All RSDs within the collection will be imported.\n\n" +
    "Please confirm that the RSDs are not similar to those already in your library."

  @Input() searchResult?: ApiCollectionSearchResult
  @Input() id = "collection-list-row"
  @Input() nextId = ""

  imported = false

  externalLinkIcon = SvgHelper.path(SvgIcon.EXTERNAL_LINK)

  get collection(): ApiCollectionSummary | undefined {
    return this.searchResult?.collection
  }

  constructor(
    protected collectionService: CollectionService,
    protected toastService: ToastService
  ) { }

  ngOnInit(): void {
    if (!this.id) {
      throw Error()
    }
  }

  onImportCollectionClicked(): void {
    if (this.collection && this.collection.libraryName) {
      if (confirm(ExternalSearchCollectionRowComponent.IMPORT_CONFIRMATION_MSG)) {
        this.collectionService.importCollection(
          this.collection.id,
          this.collection.libraryName
        ).subscribe(resp => {
          this.imported = true
          this.toastService.showToast(
            ExternalSearchCollectionRowComponent.IMPORT_SUCCESS_TITLE,
            ExternalSearchCollectionRowComponent.IMPORT_SUCCESS_MSG
          )
        })
      }
    }
  }
}
