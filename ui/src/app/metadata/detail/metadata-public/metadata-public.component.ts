import { Component, Input } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute } from "@angular/router";

import { IDetailCardSectionData } from "src/app/detail-card/section/section.component";
import { AbstractMetadataDetailComponent } from "../abstract-metadata-detail.component";
import { ApiJobCode } from "../../job-codes/Jobcode";
import { ApiNamedReference } from "../../named-references/NamedReference";
import { MetadataType } from "../../rsd-metadata.enum";


@Component({
  selector: "app-metadata-public",
  templateUrl: "./metadata-public.component.html"
})
export class MetadataPublicComponent extends AbstractMetadataDetailComponent {

  constructor(
    protected route: ActivatedRoute,
    protected titleService: Title
  ) {
    super(
      route,
      titleService
    );
  }

  ngOnInit(): void {
    this.loadMetadata();
  }
}