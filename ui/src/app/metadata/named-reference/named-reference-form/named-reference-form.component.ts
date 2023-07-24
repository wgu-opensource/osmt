import { Component } from '@angular/core';
import { MetadataFormComponent } from "../../form/metadata-form.component"
import { FormBuilder, FormGroup, Validators } from "@angular/forms"
import { ActivatedRoute, Router } from "@angular/router"
import { Location } from "@angular/common"
import { NamedReferenceService } from "../service/named-reference.service"

@Component({
  selector: 'app-create',
  templateUrl: './named-reference-form.component.html',
  styleUrls: ['./named-reference-form.component.scss']
})
export class NamedReferenceFormComponent extends MetadataFormComponent {

  constructor(
    protected route: ActivatedRoute,
    protected router: Router,
    protected location: Location,
    protected formBuilder: FormBuilder,
    protected namedReferenceService: NamedReferenceService
  ) {
    super(route, router, location, formBuilder, namedReferenceService)
  }

  getFormDefinitions(): FormGroup {
    return this.formBuilder.group({
      name: [undefined, [Validators.required]],
      url: [undefined],
      type: [this.metadataType],
      framework: [undefined]
    })
  }

}
