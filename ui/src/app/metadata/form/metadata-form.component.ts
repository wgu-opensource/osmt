import { Location } from "@angular/common"
import { Component, Input } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";

import { Observable } from "rxjs";

import { AbstractDataService } from "src/app/data/abstract-data.service";
import { ApiJobCode } from "../job-code/Jobcode";
import { ApiNamedReference } from "../named-reference/NamedReference";
import { MetadataType } from "../rsd-metadata.enum";


@Component({
  selector: "app-metadata-form",
  templateUrl: "./metadata-form.component.html"
})
export class MetadataFormComponent {
  metadataForm = new FormGroup(this.getFormDefinitions());
  alignmentForms: FormGroup[] = [];
  id: number;
  metadata?: ApiNamedReference | ApiJobCode;

  metadataLoaded: Observable<any> | null = null
  metadataSaved: Observable<any> | null = null

  constructor(
    protected route: ActivatedRoute,
    private router: Router,
    private location: Location,
    protected metadataService: AbstractDataService
  ) {
    // this.id = parseInt(this.route.snapshot.paramMap.get("id") ?? "-1");
    this.id = 2998
  }

  get nameErrorMessage(): string {
    return "Metadata name required";
  }

  get metadataErrorMessage(): string {
    return "Metadata information required";
  }

  get formValid(): boolean {
    const alignments_valid = !this.alignmentForms.map(group => group.valid).some(x => !x)
    return alignments_valid && this.metadataForm.valid
  }

  get formDirty(): boolean {
    const alignments_dirty = this.alignmentForms.map(x => x.dirty).some(x => x);
    return alignments_dirty || this.metadataForm.dirty;
  }

  ngOnInit(): void {
    if (this.id) {
      this.metadataLoaded = this.metadataService.getDataById(this.id);
      this.metadataLoaded.subscribe(metadata => { this.setMetadata(metadata) });
    }
  }

  pageTitle(): string {
    return `${this.metadata ? "Edit" : "Create"} ${this.getMetadataType()}`;
  }

  setMetadata(data: ApiNamedReference | ApiJobCode) {
    this.metadata = data;
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

  getFormDefinitions() {
    const fields = {
      metadataName: new FormControl(null),
      metadataValue: new FormControl(null)
    }

    return fields;
  }

  handleFormErrors(errors: unknown): void {
  }

  handleClickCancel(): boolean {
    this.location.back()
    return false
  }

  updateObject() {
    return [];
  }

  onSubmit(): void {
    const updateObject = this.updateObject();

    if (Object.keys(updateObject).length < 1) {
      return;
    }

    // if (this.id) {
    //   this.metadataSaved = this.metadataService.updateMetadata(this.id, updateObject);
    // } else {
    //   this.metadataSaved = this.metadataService.createMetadata(updateObject);
    // }

    if (this.metadataSaved) {
      this.metadataSaved.subscribe((result) => {
        if (!result) {
          return;
        }

        // this.metadataForm.markAsPristine();
        
        this.router.navigate([`/metadata`]);
      });
    }
  }
}