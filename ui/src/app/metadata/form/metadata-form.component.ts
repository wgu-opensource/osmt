import { Location } from "@angular/common"
import { Component, OnInit } from "@angular/core"
import { FormBuilder, FormGroup } from "@angular/forms"
import { ActivatedRoute, Router } from "@angular/router";

import { Observable } from "rxjs";

import { ApiJobCode } from "../job-code/Jobcode";
import { ApiNamedReference } from "../named-reference/NamedReference";
import { MetadataType } from "../rsd-metadata.enum";
import { AbstractDataService } from "../../data/abstract-data.service"

@Component({
  selector: "app-metadata-form",
  template: ""
})
export abstract class MetadataFormComponent implements OnInit {

  metadataForm!: FormGroup
  alignmentForms: FormGroup[] = [];
  id: number;
  metadata?: ApiNamedReference | ApiJobCode;
  metadataType?: string

  metadataLoaded: Observable<any> | null = null
  metadataSaved: Observable<any> | null = null

  protected constructor(
    protected route: ActivatedRoute,
    protected router: Router,
    protected location: Location,
    protected formBuilder: FormBuilder,
    protected metadataService: AbstractDataService
  ) {
    const state: any = this.router.getCurrentNavigation()?.extras.state
    if (state) {
      this.metadataType = Object.keys(MetadataType)[Object.values(MetadataType).indexOf(state.metadataType)];
    }
    console.log(this.metadataType)
    this.metadataForm = this.getFormDefinitions()
    this.id = parseInt(this.route.snapshot.paramMap.get("id") ?? "-1");
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
    if (this.id > 0) {
      this.metadataLoaded = this.metadataService.getById(this.id);
      this.metadataLoaded.subscribe(metadata => {
        this.setMetadata(metadata)
        this.metadataForm.patchValue(metadata)
      });
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

  abstract getFormDefinitions(): FormGroup

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

    // if (Object.keys(updateObject).length < 1) {
    //  return;
    // }

    if (this.id > 0) {
      this.metadataSaved = this.metadataService.update(this.id, this.metadataForm.value as any);
    } else {
      this.metadataSaved = this.metadataService.create(this.metadataForm.value as any);
    }

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
