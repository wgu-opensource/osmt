import { Component, Input } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router";

import { AbstractDataService } from "src/app/data/abstract-data.service";
import { RichSkillService } from "src/app/richskill/service/rich-skill.service";
import { ToastService } from "src/app/toast/toast.service";
import { AbstractMetadataDetailComponent } from "../abstract-metadata-detail.component";


@Component({
  selector: "app-metadata-public",
  templateUrl: "./metadata-public.component.html"
})
export class MetadataPublicComponent extends AbstractMetadataDetailComponent {

  constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected metadataService: AbstractDataService,
    protected skillService: RichSkillService,
    protected titleService: Title,
    protected toastService: ToastService
  ) {
    super(
      router,
      route,
      metadataService,
      skillService,
      titleService,
      toastService
    );
  }

  ngOnInit(): void {
    this.loadMetadata();
  }
}
