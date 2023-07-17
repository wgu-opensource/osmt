import { Component } from '@angular/core';
import { MetadataFormComponent } from "../../form/metadata-form.component"
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms"
import { ActivatedRoute, Router } from "@angular/router"
import { Location } from "@angular/common"
import { NamedReferenceService } from "../service/named-reference.service"
import { ExtrasSelectedSkillsState } from "../../../collection/add-skills-collection.component"

@Component({
  selector: 'app-create',
  templateUrl: './create-named-reference.component.html',
  styleUrls: ['./create-named-reference.component.scss']
})
export class CreateNamedReferenceComponent extends MetadataFormComponent {

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
