import {Component, OnInit} from "@angular/core"
import {ApiCollection} from "../ApiCollection";
import {ApiSearch, ApiSkillListUpdate} from "../../richskill/service/rich-skill-search.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CollectionService} from "../service/collection.service";
import {ToastService} from "../../toast/toast.service";
import {SkillsListComponent} from "../../richskill/list/skills-list.component";
import {RichSkillService} from "../../richskill/service/rich-skill.service";
import {FormControl, FormGroup} from "@angular/forms";
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions";
import {PublishStatus} from "../../PublishStatus";
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary";

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

  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unarchived, PublishStatus.Published, PublishStatus.Archived])

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  uuidParam?: string

  showAddToCollection = false
  showingMultipleConfirm = false

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

  actionDefinitions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Edit Collection Name",
        icon: this.editIcon,
        callback: () => this.editAction()
      }),
      new TableActionDefinition({
        label: "Publish Collection ",
        icon: this.publishIcon,
        callback: () => this.publishAction(),
        visible: () => this.collection?.status !== PublishStatus.Published
      }),
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
      new TableActionDefinition({
        label: "Add Skills to This Collection",
        icon: this.addIcon,
        callback: () => this.addSkillsAction(),
      }),
    ]
  }

  editAction(): void {
    this.router.navigate([`/collections/${this.uuidParam}/edit`])
  }

  publishAction(): void {}
  archiveAction(): void {}
  unarchiveAction(): void {}
  addSkillsAction(): void {}

  removeFromCollection(skill?: ApiSkillSummary): void {
    this.apiSearch = this.getApiSearch(skill)
    if (this.uuidParam === undefined) {
      return
    }

    const first = this.getSelectedSkills(skill)?.find(it => true)

    const count = (this.apiSearch?.uuids?.length ?? 0)
    if (count > 1) {
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
      this.toastService.showToast("Success!", `You removed 1 RSD from this collection.`)
      this.toastService.hideBlockingLoader()
      this.loadNextPage()
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
}
