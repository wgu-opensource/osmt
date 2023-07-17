import { Component, OnInit } from "@angular/core"
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router";

import { RichSkillService } from "src/app/richskill/service/rich-skill.service";
import { ToastService } from "src/app/toast/toast.service";
import { AbstractMetadataDetailComponent } from "../abstract-metadata-detail.component";
import { NamedReferenceService } from "../../named-reference/service/named-reference.service"


@Component({
  selector: "app-metadata-manage",
  templateUrl: "./metadata-manage.component.html"
})
export class MetadataManageComponent extends AbstractMetadataDetailComponent implements OnInit {

  constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected metadataService: NamedReferenceService,
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
