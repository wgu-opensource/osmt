import { Component, Input } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";

import { ApiJobCode } from "../../job-code/Jobcode";
import { ApiNamedReference } from "../../named-reference/NamedReference";
import { RichSkillService } from "../../../richskill/service/rich-skill.service";
import { MetadataType } from "../../rsd-metadata.enum";
import { ToastService } from "../../../toast/toast.service";

@Component({
  selector: "app-metadata-card",
  templateUrl: "./metadata-card.component.html"
})
export class MetadataCardComponent {

  @Input() metadata: ApiNamedReference | ApiJobCode | undefined;
  @Input() indexOfFirstSkill: number | undefined = undefined;
  @Input() currentOnPage: number | undefined = undefined;
  @Input() showSkillCount: boolean = true;

  protected metadataType: string;

  constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService
  ) {
    this.metadataType = this.getMetadataType();
  }

  get metadataName(): string {
    return (this.metadata as ApiNamedReference)?.name ?? "";
  }

  get metadataSkillsLabel(): string {
    return "random label?";
    // let label = (this.showSkillCount && this.metadataSkillCount) ?
    //   `${this.categorySkillCount} RSD${(this.metadata?.skillCount != 1) ? 's' : ''} with category.` : ""

    // label = (this.firstSkillIndex && this.lastSkillIndex) ?
    //   `${label} Viewing ${this.firstSkillIndex}-${this.lastSkillIndex}.` : label

    // return label
  }

  // get metadataSkillCount(): string | undefined {
  //   return (this.category?.skillCount) ? this.category.skillCount.toString() : undefined
  // }

  get firstSkillIndex(): string | undefined {
    return (this.indexOfFirstSkill) ? `${this.indexOfFirstSkill + 1}` : undefined;
  }

  get lastSkillIndex(): string | undefined  {
    return (this.indexOfFirstSkill && this.currentOnPage) ? `${this.indexOfFirstSkill + this.currentOnPage}` : undefined;
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
}
