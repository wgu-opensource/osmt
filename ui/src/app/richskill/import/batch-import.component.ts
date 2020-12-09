import {Component, OnInit} from "@angular/core";
import {QuickLinksHelper} from "../../core/quick-links-helper";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {Location} from "@angular/common";
import {FormControl} from "@angular/forms";
import {Papa, ParseResult} from "ngx-papaparse";


export enum ImportStep {
  UploadFile = 1,
  FieldMapping = 2,
  ReviewRecords = 3,
  Success = 4
}

@Component({
  selector: "app-batch-import",
  templateUrl: "./batch-import.component.html"
})
export class BatchImportComponent extends QuickLinksHelper implements OnInit {

  currentStep: ImportStep = ImportStep.UploadFile

  fileInput: FormControl = new FormControl("")
  uploadedFile: any;
  uploadedFileError: boolean = false

  acceptableFileTypes = [
    "text/csv",
  ]
  parseResults?: ParseResult

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected route: ActivatedRoute,
              protected location: Location,
              protected papa: Papa
  ) {
    super()
  }

  ngOnInit(): void {
  }

  resetState(): void {
    this.currentStep = ImportStep.UploadFile
    this.uploadedFile = undefined
    this.uploadedFileError = false
    this.parseResults = undefined
  }

  stepName(stepNo: ImportStep): string {
    switch (stepNo) {
      case ImportStep.UploadFile: return "Select File"
      case ImportStep.FieldMapping: return "Map Fields"
      case ImportStep.ReviewRecords: return "Review and Import"
      case ImportStep.Success: return "Success!"
      default: return ""
    }
  }

  isUploadStep(): boolean { return this.currentStep === ImportStep.UploadFile }
  isMappingStep(): boolean { return this.currentStep === ImportStep.FieldMapping }
  isReviewStep(): boolean { return this.currentStep === ImportStep.ReviewRecords }
  isSuccessStep(): boolean { return this.currentStep === ImportStep.Success }

  get nextButtonLabel(): string {
    switch (this.currentStep) {
      case ImportStep.ReviewRecords: return "Import"
      default: return "Next"
    }
  }
  get cancelButtonLabel(): string {
    switch (this.currentStep) {
      case ImportStep.FieldMapping:
      case ImportStep.ReviewRecords: return "Cancel Import"
      default: return "Cancel"
    }
  }

  handleClickNext(): boolean {
    this.currentStep += 1
    return false
  }

  handleClickCancel(): boolean {
    this.location.back()
    return false
  }

  isStepValid(): boolean {
    switch (this.currentStep) {
      case ImportStep.UploadFile: return this.parseResults !== undefined
      default: return true
    }
  }

  handleFileChange($event: Event): void {
    const target = $event.target as HTMLInputElement

    console.log("file changed", $event, target.files)

    if (target.files && target.files.length > 0) {
      const file = target.files[0]

      this.uploadedFile = file.name

      console.log("file type", file.type)
      if (this.acceptableFileTypes.indexOf(file.type) === -1) {
        this.uploadedFile = "The file you select must be CSV format."
        this.uploadedFileError = true
        return
      }

      this.papa.parse(file, {
        header: true,
        complete: (results) => {
          this.uploadedFileError = false
          this.parseResults = results
          console.log("parsed", results)
        }
      })
    }
  }

  get uploadedFileCount(): number {
    if (this.uploadedFile) {
      return 1
    }
    return 0
  }
}

