import { Location } from "@angular/common";
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";

import { JobCodeService } from "../service/job-code.service";
import { MetadataFormComponent } from "../../form/metadata-form.component";

@Component({
  selector: 'app-create',
  templateUrl: './job-code-form.component.html'
})
export class JobCodeFormComponent extends MetadataFormComponent {

  constructor(
    protected route: ActivatedRoute,
    protected router: Router,
    protected location: Location,
    protected formBuilder: FormBuilder,
    protected jobCodeService: JobCodeService
  ) {
    super(route, router, location, formBuilder, jobCodeService);
  }

  get jobCodeErrorMessage(): string {
    return "Job code required";
  }

  getFormDefinitions(): FormGroup {
    return this.formBuilder.group({
      code: [undefined, [Validators.required]],
      name: [undefined, [Validators.required]],
      framework: [undefined],
      description: [undefined, [Validators.required]],
      url: [undefined],
      type: [this.metadataType],
    });
  }

}
