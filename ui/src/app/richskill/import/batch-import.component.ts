import {Component, OnInit} from "@angular/core";
import {QuickLinksHelper} from "../../core/quick-links-helper";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {Location} from "@angular/common";
import {Papa, ParseResult} from "ngx-papaparse";


export enum ImportStep {
  UploadFile = 1,
  FieldMapping = 2,
  ReviewRecords = 3,
  Success = 4
}

export const importSkillHeaders: {[p: string]: string} = {
  skillName: "RSD Name",
  skillStatement: "Skill Statement",
  category: "Category",
}


@Component({
  selector: "app-batch-import",
  templateUrl: "./batch-import.component.html"
})
export class BatchImportComponent extends QuickLinksHelper implements OnInit {

  currentStep: ImportStep = ImportStep.UploadFile

  uploadedFile: any;
  uploadedFileError: boolean = false

  acceptableFileTypes = [
    "text/csv",
  ]
  parseResults?: ParseResult
  fieldMappings?: {[p: string]: string}

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

  get recordCount(): number {
    return this.parseResults?.data?.length ?? 0
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
      case ImportStep.FieldMapping: return this.isMappingValid()
      default: return true
    }
  }

  isMappingValid(): boolean {
    if (this.fieldMappings === undefined) { return false }
    const mapped = Object.values(this.fieldMappings)
    if (mapped.indexOf("skillName") === -1 || mapped.indexOf("skillStatement") === -1) {
      return false
    }

    if ((this.duplicateMappings()?.length ?? 0) > 0) {
      return false
    }

    return true
  }

  handleFileChange($event: Event): void {
    const target = $event.target as HTMLInputElement

    if (target.files && target.files.length > 0) {
      const file = target.files[0]

      this.uploadedFile = file.name

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

  uploadedHeaders(): string[] {
    const data = this.parseResults?.data
    if (!data || data.length < 1) { return [] }
    return Object.keys(data[0])
  }

  handleMappingChanged(fieldMappings: {[p: string]: string}): void {
    this.fieldMappings = fieldMappings

  }

  get hasMappingError(): boolean {
    return (this.duplicateMappings()?.length  ?? 0) > 0
  }

  duplicateMappings(): string[] | undefined {
    if (this.fieldMappings !== undefined) {
      const count: {[key: string]: number} = {}
      Object.values(this.fieldMappings).forEach(it => {
        count[it] = (count[it] ?? 0) + 1
      })
      const duplicates: string[] = Object.entries(count).filter(it => it[1] > 1).map(it => it[0])
      if (duplicates.length > 0) {
        return duplicates
      }
    }

    return undefined
  }

  duplicateFieldNames(): string {
    const words = this.duplicateMappings()?.map(it => `"${importSkillHeaders[it]}"`)
    if (words === undefined) { return "" }

    if (words.length > 1) {
      return `${words.slice(0, -1).join(", ")}, and ${words.slice(-1)}`
    }

    return words[0]
  }
}

