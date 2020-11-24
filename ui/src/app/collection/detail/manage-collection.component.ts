import {Component, OnInit} from "@angular/core"
import {ApiCollection, ApiCollectionUpdate} from "../ApiCollection";
import {ApiSearch, ApiSkillListUpdate} from "../../richskill/service/rich-skill-search.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CollectionService} from "../service/collection.service";
import {ToastService} from "../../toast/toast.service";
import {SkillsListComponent} from "../../richskill/list/skills-list.component";
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {FormControl, FormGroup} from "@angular/forms";
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions";
import {determineFilters, PublishStatus} from "../../PublishStatus";
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary";
import {Observable} from "rxjs";

@Component({
  selector: "app-manage-collection",
  templateUrl: "./manage-collection.component.html"
})
export class ManageCollectionComponent extends SkillsListComponent implements OnInit {
  collection?: ApiCollection
  apiSearch?: ApiSearch

  editIcon = SvgHelper.path(SvgIcon.EDIT)
  publishIcon = SvgHelper.path(SvgIcon.PUBLISH)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)
  unarchiveIcon = SvgHelper.path(SvgIcon.UNARCHIVE)
  addIcon = SvgHelper.path(SvgIcon.ADD)

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Draft, PublishStatus.Published, PublishStatus.Archived])

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  uuidParam?: string

  showAddToCollection = false
  showingMultipleConfirm = false
  collectionSaved?: Observable<ApiCollection>
  selectAllChecked: boolean = false

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
    this.uuidParam = this.route.snapshot.paramMap.get("uuid") ?? ""
    this.collectionService.getCollectionByUUID(this.uuidParam).subscribe(collection => {
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
      determineFilters(this.selectedFilters),
      this.columnSort,
      this.apiSearch
    )
    this.resultsLoaded.subscribe(results => this.setResults(results))
  }

  getSelectAllCount(): number {
    return this.totalCount
  }

  handleSelectAll(selectAllChecked: boolean): boolean {
    this.selectAllChecked = selectAllChecked
    return false
  }

  get selectedCount(): number {
    return this.selectAllChecked ? this.totalCount : (this.selectedSkills?.length ?? 0)
  }

  public get searchQuery(): string {
    return this.searchForm.get("search")?.value.trim() ?? ""
  }
  clearSearch(): boolean {
    this.searchForm.reset()
    this.apiSearch = undefined
    this.from = 0
    this.loadNextPage()
    return false
  }
  handleDefaultSubmit(): boolean {
    if (this.searchQuery.length > 0) {
      this.apiSearch = new ApiSearch({query: this.searchQuery})
      this.matchingQuery = [this.searchQuery]
      this.from = 0
      this.loadNextPage()
    }
    return false
  }

  actionDefinitions(): TableActionDefinition[] {
    const actions = [
      new TableActionDefinition({
        label: "Edit Name and Author",
        icon: this.editIcon,
        callback: () => this.editAction()
      })
    ]

    if (this.collection?.publishDate) {
      actions.push(new TableActionDefinition({
        label: "View Published Collection",
        icon: this.publishIcon,
        callback: () => this.viewPublishedAction(),
      }))
    } else {
      actions.push(new TableActionDefinition({
        label: "Publish Collection",
        icon: this.publishIcon,
        callback: () => this.publishAction(),
      }))
    }

    actions.push(
      new TableActionDefinition({
        label: "Archive Collection ",
        icon: this.archiveIcon,
        callback: () => this.archiveAction(),
        visible: () => this.collection?.status !== PublishStatus.Archived
      }),
      new TableActionDefinition({
        label: "Un-archive Collection ",
        icon: this.unarchiveIcon,
        callback: () => this.unarchiveAction(),
        visible: () => this.collection?.status === PublishStatus.Archived
      }),
      // new TableActionDefinition({
      //   label: "Add RSDs to This Collection",
      //   icon: this.addIcon,
      //   callback: () => this.addSkillsAction(),
      // }),
    )
    return actions
  }

  editAction(): void {
    this.router.navigate([`/collections/${this.uuidParam}/edit`])
  }

  viewPublishedAction(): void {
    const url = `/collections/${this.uuidParam}`
    window.open(url, "_blank")
  }


  publishAction(): void {
    if (this.uuidParam === undefined) { return }

    // TODO: once OSMT-326 is complete, re-enable publishing guards
    if (confirm("Confirm that you want to publish the selected collection. Once published, a collection can't be unpublished.")) {
      this.submitCollectionStatusChange(PublishStatus.Published, "published")
    }

    // this.toastService.showBlockingLoader()
    // this.collectionService.collectionReadyToPublish(this.uuidParam).subscribe(ready => {
    //   this.toastService.hideBlockingLoader()
    //   if (ready) {
    //       if (confirm("Confirm that you want to publish the selected collection. Once published, a collection can't be unpublished.")) {
    //         this.submitCollectionStatusChange(PublishStatus.Published, "published")
    //       }
    //   } else {
    //     this.router.navigate([`/collections/${this.uuidParam}/publish`])
    //   }
    // })
  }

  archiveAction(): void {
    this.submitCollectionStatusChange(PublishStatus.Archived, "archived")
  }
  unarchiveAction(): void {
    this.submitCollectionStatusChange(PublishStatus.Unarchived, "unarchived")
  }

  submitCollectionStatusChange(newStatus: PublishStatus, verb: string): void {
    if (this.uuidParam === undefined) { return }

    const updateObject = new ApiCollectionUpdate({status: newStatus})

    this.toastService.showBlockingLoader()
    this.collectionSaved = this.collectionService.updateCollection(this.uuidParam, updateObject)
    this.collectionSaved.subscribe((collection) => {
      this.toastService.hideBlockingLoader()
      this.toastService.showToast("Success!", `You ${verb} the collection ${collection.name}.`)
      this.collection = collection
      this.loadNextPage()
    })
  }

  addSkillsAction(): void {}


  getApiSearch(skill?: ApiSkillSummary): ApiSearch | undefined {
    if (this.selectAllChecked) {
      return new ApiSearch({
        query: this.searchQuery
      })
    } else {
      return super.getApiSearch(skill)
    }
  }

  removeFromCollection(skill?: ApiSkillSummary): void {
    if (this.uuidParam === undefined) { return }

    this.apiSearch = this.getApiSearch(skill)

    const first = this.getSelectedSkills(skill)?.find(it => true)

    const count = (this.apiSearch?.uuids?.length ?? 0)

    if (count > 1 || this.selectAllChecked) {
      this.showingMultipleConfirm = true
    } else {
      if (confirm(`Confirm that you want to remove the following RSD from this collection.\n${first?.skillName}`)) {
        this.submitSkillRemoval(this.apiSearch)
      }
    }



  }

  submitSkillRemoval(apiSearch?: ApiSearch): void {
    const update = new ApiSkillListUpdate({remove: apiSearch})
    this.toastService.showBlockingLoader()
    this.skillsSaved = this.collectionService.updateSkillsWithResult(this.uuidParam!, update)
    this.skillsSaved.subscribe(result => {
      if (result) {
        this.toastService.showToast("Success!", `You removed ${result.modifiedCount} RSD${(result.modifiedCount ?? 0) > 1 ? "s" : ""} from this collection.`)
        this.toastService.hideBlockingLoader()
        this.loadNextPage()
      }
    })

  }

  handleClickConfirmMulti(): boolean {
    this.submitSkillRemoval(this.apiSearch)
    this.showingMultipleConfirm = false
    this.apiSearch = undefined
    return false
  }

  handleClickCancel(): boolean {
    this.showingMultipleConfirm = false
    this.apiSearch = undefined
    return false
  }

  protected handleClickBackToTop(action: TableActionDefinition, skill?: ApiSkillSummary): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement, "h2")
    return false
  }
}
