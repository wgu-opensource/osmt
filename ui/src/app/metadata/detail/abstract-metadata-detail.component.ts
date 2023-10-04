import { Component, ElementRef, OnInit, ViewChild} from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router";
import { SvgHelper, SvgIcon } from "src/app/core/SvgHelper";
import { AbstractDataService } from "src/app/data/abstract-data.service";
import { IDetailCardSectionData } from "src/app/detail-card/section/section.component";
import { FilterDropdown } from "src/app/models";
import { PublishStatus } from "src/app/PublishStatus";
import { ApiSortOrder } from "src/app/richskill/ApiSkill";
import { RichSkillService } from "src/app/richskill/service/rich-skill.service";
import { RelatedSkillTableControl } from "src/app/table/control/related-skill-table.control";
import { ISkillTableControl } from "src/app/table/control/table.control";
import { TableActionDefinition } from "src/app/table/skills-library-table/has-action-definitions";
import { ToastService } from "src/app/toast/toast.service";
import { ApiJobCode } from "../job-code/Jobcode";
import { ApiNamedReference } from "../named-reference/NamedReference";
import { MetadataType } from "../rsd-metadata.enum";
import { QuickLinksHelper } from "../../core/quick-links-helper";
import { ApiSkillSummary } from "../../richskill/ApiSkillSummary"


@Component({
  template: ``
})
export abstract class AbstractMetadataDetailComponent extends QuickLinksHelper implements OnInit {

  @ViewChild("titleHeading") titleElement!: ElementRef
  idParam: number | null;
  metadata?: ApiNamedReference | ApiJobCode;
  skillTableControl: RelatedSkillTableControl<number>

  searchForm = new FormGroup({
    search: new FormControl("")
  })
  skills: ApiSkillSummary[] = []

  readonly searchIcon = SvgHelper.path(SvgIcon.SEARCH)

  protected constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected metadataService: AbstractDataService,
    protected skillService: RichSkillService,
    protected titleService: Title,
    protected toastService: ToastService
  ) {
    super();
    this.idParam = parseInt(this.route.snapshot.paramMap.get("id") ?? "-1")

    this.skillTableControl = new RelatedSkillTableControl<number>(
      metadataService,
      {
        from: 0,
        size: 50,
        sort: ApiSortOrder.NameAsc,
        statusFilters: new Set([PublishStatus.Draft, PublishStatus.Published])
      } as ISkillTableControl
    );
  }

  get showLibraryEmptyMessage(): boolean {
    return true;
  }

  get showSkillsEmpty(): boolean {
    return !this.showSkillsTable;
  }

  get showSkillsFilters(): boolean {
    return true;
  }

  get showSkillsLoading(): boolean {
    return false;
  }

  get showSkillsTable(): boolean {
    return this.skillTableControl.skills.length > 0
  }

  get skillsCountLabel(): string {
    const rsdLabel = (this.skillTableControl.size == 1) ? "RSD" : "RSDs"
    return `${this.skillTableControl.totalCount} ${rsdLabel} with this ${this.getMetadataType()} based on`
  }

  get skillsViewingLabel(): string {
    return (this.skillTableControl.currFirstSkillIndex && this.skillTableControl.currLastSkillIndex)
      ? `Viewing ${this.skillTableControl.currFirstSkillIndex}-${this.skillTableControl.currLastSkillIndex}` : "";
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
    ];
  }

  protected get searchFieldValue(): string | undefined {
    const value = this.searchForm.get("search")?.value?.trim();
    return (value && value.length > 0) ? value : undefined;
  }


  ngOnInit(): void {
    this.loadMetadata();
  }

  protected loadMetadata() {
    if (this.idParam) {
      this.metadataService.getById(this.idParam).subscribe((m: ApiNamedReference | ApiJobCode) => this.setMetadata(m));
    } else {
      this.clearMetadata();
    }
  }

  protected getMetadata() {
    return this.metadata;
  }

  protected setMetadata(metadata: ApiNamedReference | ApiJobCode) {
    this.metadata = metadata;
    console.log(this.metadata)

    // this.titleService.setTitle(`${category.name} | Category | ${this.whitelabel.toolName}`)
    this.loadSkills();
  }

  protected loadSkills(): void {
    if (this.metadata) {
      this.skillTableControl.loadSkills(this.idParam ?? -1)
    } else {
      this.clearSkills();
    }
  }

  protected clearMetadata() {
    this.metadata = undefined;
    this.titleService.setTitle(
      `${this.getMetadataType()} | ${this.whitelabel.toolName}`);
    this.loadSkills();
  }

  protected clearSkills() {
    // this.skillTableControl.clearSkills();
  }

  getId(): number {
    return this.metadata?.id ?? -1;
  }

  getPublicUrl(): string {
    return (this.metadata as any)?.publicUrl ?? "";
  }

  getCardFormat(): IDetailCardSectionData[] {
    return [];
  }

  getMetadataName(): string {
    return( this.metadata as ApiNamedReference)?.name ?? ""
  }

  getMetadataType(): string {
    if (this.metadata instanceof ApiNamedReference) {
      return this.metadata.type ?? "";
    }
    else if (this.metadata instanceof ApiJobCode) {
      return MetadataType.JobCode;
    }
    else {
      return "";
    }
  }

  getMobileSkillSortOptions(): {[s: string]: string} {
    return {
      "skill.asc": "RSD Name (ascending)",
      "skill.desc": "RSD Name (descending)",
    };
  }

  navigateToPage(newPageNo: number) {
    this.skillTableControl.from = (newPageNo - 1) * this.skillTableControl.size;
    this.loadSkills();
  }

  clearSearch(): boolean {
    this.searchForm.reset();
    this.skillTableControl.query = undefined;
    this.skillTableControl.from = 0;
    this.loadSkills();
    return false;
  }

  handleClickBackToTop(action: TableActionDefinition): boolean {
    this.focusAndScrollIntoView(this.titleElement.nativeElement);
    return false;
  }

  handleHeaderColumnSort(sort: ApiSortOrder) {
    this.skillTableControl.sort = sort;
    this.skillTableControl.from = 0;
    this.loadSkills();
  }

  handlePageClicked(newPageNo: number) {
    this.navigateToPage(newPageNo);
  }

  handleStatusFilterChange(filters: Set<PublishStatus>) {
    this.skillTableControl.statusFilters = filters;
    this.loadSkills();
  }

  handleKeywordFilterChange(filters: FilterDropdown) {
    this.skillTableControl.keywordFilters = filters;
    this.loadSkills();
  }

  handleSearchSubmit(): boolean {
    this.skillTableControl.query = this.searchFieldValue;
    this.skillTableControl.from = 0;
    this.loadSkills();
    return false;
  }

}
