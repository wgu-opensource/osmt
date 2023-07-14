import { Component, OnInit, ViewChild } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router"
import { IDetailCardSectionData } from "src/app/detail-card/section/section.component";
import { ApiJobCode } from "../job-code/Jobcode";
import { ApiNamedReference } from "../named-reference/NamedReference";
import { MetadataType } from "../rsd-metadata.enum";
import { QuickLinksHelper } from "../../core/quick-links-helper";
import { AbstractDataService } from "../../data/abstract-data.service"
import { RichSkillService } from "../../richskill/service/rich-skill.service"
import { ToastService } from "../../toast/toast.service"


@Component({
  template: ``
})
export abstract class AbstractMetadataDetailComponent extends QuickLinksHelper implements OnInit {

  @ViewChild("titleHeading") titleElement!: ElementRef
  idParam: string | null;
  metadata?: ApiNamedReference | ApiJobCode;

  protected constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected metadataService: AbstractDataService,
    protected skillService: RichSkillService,
    protected titleService: Title,
    protected toastService: ToastService
  ) {
    super();
    this.idParam = this.route.snapshot.paramMap.get("id");
  }

  ngOnInit(): void {

  }

  protected loadMetadata() {
    if (this.idParam) {
      // this.categoryService.getById(this.idParam).subscribe(
      //   (m: ApiNamedReference | ApiJobCode) => this.setMetadata(m));
    } else {
      this.clearMetadata();
    }
  }

  protected getMetadata() {
    return this.metadata;
  }

  protected setMetadata(metadata: ApiNamedReference | ApiJobCode) {
    this.metadata = metadata;

    // this.titleService.setTitle(`${category.name} | Category | ${this.whitelabel.toolName}`)
    this.loadSkills();
  }

  protected loadSkills(): void {
    if (this.metadata) {
      // this.skillTableControl.loadSkills(this.metadata.id);
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

  getCardFormat(): IDetailCardSectionData[] {
    return [];
  }

  getMetadataName(): string {
    return "Fine Arts";
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
