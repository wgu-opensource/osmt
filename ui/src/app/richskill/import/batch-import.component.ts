import {Component, OnInit} from "@angular/core";
import {QuickLinksHelper} from "../../core/quick-links-helper";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {Location} from "@angular/common";
import {Papa, ParseResult} from "ngx-papaparse";
import {ApiNamedReference} from "../ApiSkill"
import {ApiSkillUpdate, ApiStringListUpdate, IRichSkillUpdate} from "../ApiSkillUpdate";


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
  keywords: "Keywords",
  author: "Author",
  standards: "Standards",
  certifications: "Certifications",
  occupations: "Occupations",
  employers: "Employers",
  alignmentName: "Alignment Name",
  alignmentUrl: "Alignment URL",
}


export class AuditedImportSkill {
  skill: ApiSkillUpdate
  missing: string[]

  constructor(skill: ApiSkillUpdate, missing: string[]) {
    this.skill = skill
    this.missing = missing
  }

  get nameMissing(): boolean { return !this.skill.skillName }
  get statementMissing(): boolean { return !this.skill.skillStatement }
  get authorMissing(): boolean { return !this.skill.author }

  get isError(): boolean {
    return this.nameMissing || this.statementMissing
  }
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
  previewSkills?: ApiSkillUpdate[]
  auditedSkills?: AuditedImportSkill[]
  importedSkills?: AuditedImportSkill[]

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
    switch (this.currentStep) {
      case ImportStep.ReviewRecords:
        this.auditRecords()
        break
      case ImportStep.Success:
        this.submitRecords()
        break
    }
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
    if (this.currentStep !== ImportStep.UploadFile)  {
      this.resetState()
    }

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

  skillsFromResults(): ApiSkillUpdate[] {
    const skillUpdates = this.parseResults?.data.map((row: { [x: string]: string; }) => {
      // tslint:disable-next-line:no-any
      const newSkill: {[s: string]: any} = {}

      Object.keys(row).forEach(uploadedKey => {
        const fieldName = this.fieldMappings?.[uploadedKey]
        const value: string = row[uploadedKey].trim()
        if (fieldName !== undefined && value) {

          if (["author"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiNamedReference({name: value})
          }
          else if (fieldName.endsWith("s")) {
            newSkill[fieldName] = new ApiStringListUpdate(value.split(";").map(it => it.trim()))
          } else {
            newSkill[fieldName] = value
          }
        }
      })

      return newSkill
    }).map((it: IRichSkillUpdate) => new ApiSkillUpdate(it))
    return skillUpdates
  }

  private auditRecords(): void {
    this.previewSkills = this.skillsFromResults()
    this.auditedSkills = this.previewSkills.map(skill => {
      const required = ["skillName", "skillStatement"]
      // @ts-ignore
      const missing = required.filter(it => skill[it] === undefined)
      return new AuditedImportSkill(skill, missing)
    })
  }

  private submitRecords(): void {
    if (this.auditedSkills === undefined) {
      return
    }

    this.importedSkills = this.auditedSkills?.filter(it => !it.isError)
    const skillUpdates = this.importedSkills.map(it => it.skill)

    this.toastService.showBlockingLoader()
    this.richSkillService.createSkills(skillUpdates).subscribe(results => {
      if (results) {
        this.toastService.hideBlockingLoader()
        console.log("success!", results.length)
      }
    })

  }
}

