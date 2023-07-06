import { Component, Input, OnInit} from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute } from "@angular/router";

import { IDetailCardSectionData } from "src/app/detail-card/section/section.component";
import { ApiJobCode } from "../job-codes/Jobcode";
import { ApiNamedReference } from "../named-references/NamedReference";
import { MetadataType } from "../rsd-metadata.enum";
import { QuickLinksHelper } from "../../core/quick-links-helper";

@Component({
  template: ``
})
export abstract class AbstractMetadataDetailComponent extends QuickLinksHelper implements OnInit {

  idParam: string | null;
  metadata?: ApiNamedReference | ApiJobCode;

  constructor(
    protected route: ActivatedRoute,
    protected titleService: Title
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

  getPublishStatus(): string {
    return "";
  }

  getArchivedDate(): string {
    return "";
  }

  getPublishedDate(): string {
    return "";
  }
}