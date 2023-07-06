import { Component, Input } from "@angular/core";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute } from "@angular/router";

import { IDetailCardSectionData } from "src/app/detail-card/section/section.component";
import { ApiJobCode } from "../job-codes/Jobcode";
import { ApiNamedReference } from "../named-references/NamedReference";
import { MetadataType } from "../rsd-metadata.enum";


@Component({
  selector: "app-metadata-form",
  templateUrl: "./metadata-form.component.html"
})
export class MetadataFormComponent {

  constructor() { }

  ngOnInit(): void {
  }
}