import {Component, ElementRef, OnInit, ViewChild} from "@angular/core"
import {FormControl, FormGroup} from "@angular/forms"
import {Title} from "@angular/platform-browser"
import {ActivatedRoute, Router} from "@angular/router"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {QuickLinksHelper} from "../../core/quick-links-helper"
import {CategoryService} from "../service/category.service"
import {RichSkillService} from "../../richskill/service/rich-skill.service"
import {ToastService} from "../../toast/toast.service"
import {ISkillTableControl} from "../../table/control/table.control"
import {RelatedSkillTableControl} from "../../table/control/related-skill-table.control"
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions"
import {FilterDropdown} from "../../models/filter-dropdown.model"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {PublishStatus} from "../../PublishStatus"
import {ApiCategory} from "../ApiCategory"

@Component({
  selector: "app-category-detail",
  templateUrl: "./category-detail.component.html"
})
export class CategoryDetailComponent extends QuickLinksHelper implements OnInit {
  @ViewChild("titleHeading") titleElement!: ElementRef

  title = "Category"
  idParam: string | null
  category: ApiCategory | undefined
  skillTableControl: RelatedSkillTableControl<number>

  searchForm = new FormGroup({
    search: new FormControl("")
  })

  readonly searchIcon = SvgHelper.path(SvgIcon.SEARCH)

  constructor(
    protected router: Router,
    protected categoryService: CategoryService,
    protected skillService: RichSkillService,
    protected toastService: ToastService,
    protected route: ActivatedRoute,
    protected titleService: Title
  ) {
    super()
    this.idParam = this.route.snapshot.paramMap.get("id")

    this.skillTableControl = new RelatedSkillTableControl<number>(
      categoryService,
      {
        from: 0,
        size: 50,
        sort: ApiSortOrder.NameAsc,
        statusFilters: new Set([PublishStatus.Draft, PublishStatus.Published])
      } as ISkillTableControl
    )
  }

  get showLibraryEmptyMessage(): boolean {
    return true
  }

  get showSkillsEmpty(): boolean {
    return this.skillTableControl.emptyResults
  }

  get showSkillsFilters(): boolean {
    return true
  }

  get showSkillsLoading(): boolean {
    return false
  }

  get showSkillsTable(): boolean {
    return !this.skillTableControl.emptyResults
  }

  get skillsCountLabel(): string {
      const rsdLabel = (this.category?.skillCount == 1) ? "RSD" : "RSDs"
      return `${this.skillTableControl.totalCount} ${rsdLabel} with this category based on`
  }

  get skillsViewingLabel(): string {
    return (this.skillTableControl.currFirstSkillIndex && this.skillTableControl.currLastSkillIndex)
      ? `Viewing ${this.skillTableControl.currFirstSkillIndex}-${this.skillTableControl.currLastSkillIndex}` : ""
  }

  get tableActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: (action: TableActionDefinition) => this.handleClickBackToTop(action),
        visible: () => true
      })
    ]
  }

  protected get searchFieldValue(): string | undefined {
    const value = this.searchForm.get("search")?.value?.trim()
    return (value && value.length > 0) ? value : undefined
  }

  ngOnInit(): void {
    this.loadCategory()
  }

  navigateToPage(newPageNo: number) {
    this.skillTableControl.from = (newPageNo - 1) * this.skillTableControl.size
    this.loadSkills()
  }

  clearSearch(): boolean {
    this.searchForm.reset()
    this.skillTableControl.query = undefined
    this.skillTableControl.from = 0
    this.loadSkills()
    return false
  }

  protected clearCategory() {
    this.category = undefined
    this.titleService.setTitle(`Category | ${this.whitelabel.toolName}`)
    this.loadSkills()
  }

  protected clearSkills() {
    this.skillTableControl.clearSkills()
  }

  protected setCategory(category: ApiCategory) {
    this.category = category
    this.titleService.setTitle(`${category.name} | Category | ${this.whitelabel.toolName}`)
    this.loadSkills()
  }

  protected loadCategory() {
    if (this.idParam) {
      this.categoryService.getById(this.idParam).subscribe((c: ApiCategory) => this.setCategory(c))
    } else {
      this.clearCategory()
    }
  }

  protected loadSkills(): void {
    if (this.category) {
      this.skillTableControl.loadSkills(this.category.id)
    } else {
      this.clearSkills()
    }
  }

  getMobileSortOptions(): {[s: string]: string} {
    return {
      "skill.asc": "RSD name (ascending)",
      "skill.desc": "RSD name (descending)",
    }
  }

  handleClickBackToTop(action: TableActionDefinition): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement)
    return false
  }

  handleHeaderColumnSort(sort: ApiSortOrder) {
    this.skillTableControl.sort = sort
    this.skillTableControl.from = 0
    this.loadSkills()
  }

  handlePageClicked(newPageNo: number) {
    this.navigateToPage(newPageNo)
  }

  handleStatusFilterChange(filters: Set<PublishStatus>) {
    this.skillTableControl.statusFilters = filters
    this.loadSkills()
  }

  handleKeywordFilterChange(filters: FilterDropdown) {
    this.skillTableControl.keywordFilters = filters
    this.loadSkills()
  }

  handleSearchSubmit(): boolean {
    this.skillTableControl.query = this.searchFieldValue
    this.skillTableControl.from = 0
    this.loadSkills()
    return false
  }
}
