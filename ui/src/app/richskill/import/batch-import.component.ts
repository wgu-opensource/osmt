import {Component, OnInit} from "@angular/core";
import {QuickLinksHelper} from "../../core/quick-links-helper";
import {ActivatedRoute, Router} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {ToastService} from "../../toast/toast.service";
import {Location} from "@angular/common";
import {Papa, ParseResult} from "ngx-papaparse";
import {ApiNamedReference} from "../ApiSkill"
import {ApiReferenceListUpdate, ApiSkillUpdate, ApiStringListUpdate, IRichSkillUpdate} from "../ApiSkillUpdate";
import {Observable} from "rxjs";
import {PaginatedSkills} from "../service/rich-skill-search.service";


export enum ImportStep {
  UploadFile = 1,
  FieldMapping = 2,
  ReviewRecords = 3,
  Success = 4
}


export const importSkillHeaderOrder = [
  {field: "skillName", label: "RSD Name"},
  {field: "author", label: "Author"},
  {field: "skillStatement", label: "Skill Statement"},
  {field: "category", label: "Category"},
  {field: "keywords", label: "Keywords"},
  {field: "standards", label: "Standards"},
  {field: "certifications", label: "Certifications"},
  {field: "occupations", label: "Occupations"},
  {field: "employers", label: "Employers"},
  {field: "alignmentName", label: "Alignment Name"},
  {field: "alignmentUrl", label: "Alignment URL"},
]


export const importSkillHeaders: {[p: string]: string} =
 importSkillHeaderOrder.reduce((acc: {[p: string]: string}, it) => {
   acc[it.field] = it.label
   return acc
 }, {})

export const importSkillHeadersReverse: {[p: string]: string} =
  importSkillHeaderOrder.reduce((acc: {[p: string]: string}, it) => {
    acc[it.label.toLowerCase()] = it.field
    return acc
  }, {})

export class AuditedImportSkill {
  skill: ApiSkillUpdate
  missing: string[]
  similar: boolean

  constructor(skill: ApiSkillUpdate, missing: string[], similar: boolean = false) {
    this.skill = skill
    this.missing = missing
    this.similar = similar
  }

  get nameMissing(): boolean { return !this.skill.skillName }
  get statementMissing(): boolean { return !this.skill.skillStatement }
  get authorMissing(): boolean { return !this.skill.author }

  get isError(): boolean {
    return this.nameMissing || this.statementMissing
  }
  get isWarning(): boolean {
    return this.similar
  }
}

@Component({
  selector: "app-batch-import",
  templateUrl: "./batch-import.component.html"
})
export class BatchImportComponent extends QuickLinksHelper implements OnInit {

  currentStep: ImportStep = ImportStep.UploadFile

  stepLoaded?: Observable<ImportStep>

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

  searchingSimilarity?: boolean
  similarSkills?: boolean[]
  importSimilarSkills: boolean = false

  get similarSkillCount(): number {
    return (this.similarSkills?.filter(it => it).length ?? 0)
  }

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected route: ActivatedRoute,
              protected location: Location,
              protected papa: Papa
  ) {
    super()
    this.resetState()
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
  get validCount(): number {
    return this.auditedSkills?.filter(it => !it.isError && (this.importSimilarSkills || !it.similar))?.length ?? 0
  }

  showStepLoader(): void {
    this.stepLoaded = new Observable<ImportStep>(observer => {})
  }
  hideStepLoader(): void {
    this.stepLoaded = undefined
  }

  handleClickNext(): boolean {
    this.showStepLoader()
    this.currentStep += 1
    switch (this.currentStep) {
      case ImportStep.FieldMapping:
        this.initializeMapping()
        break
      case ImportStep.ReviewRecords:
        this.auditRecords()
        break
      case ImportStep.Success:
        this.submitRecords()
        break
    }
    this.hideStepLoader()
    return false
  }

  handleClickCancel(): boolean {
    const newStep = this.currentStep - 1
    switch (this.currentStep) {
      case ImportStep.FieldMapping:
      case ImportStep.ReviewRecords: {
        this.currentStep -= 1
        break
      }
      case ImportStep.UploadFile:
      default:
        this.location.back()
        break
    }
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

  handleFileDrop($event: DragEvent): boolean {
    if (($event.dataTransfer?.files?.length ?? 0) > 0) {
      this.changeFile($event.dataTransfer?.files[0])
    }
    return false
  }
  handleFileDrag($event: DragEvent): boolean {
    $event.preventDefault()
    return false
  }

  handleFileChange($event: Event): void {
    if (this.currentStep !== ImportStep.UploadFile) {
      this.resetState()
    }

    const target = $event.target as HTMLInputElement
    if (target.files && target.files.length > 0) {
      this.changeFile(target.files[0])
    }
  }

  changeFile(file: any): void {

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

  get uploadedFileCount(): number {
    if (this.uploadedFile) {
      return 1
    }
    return 0
  }

  uploadedHeaders(): string[] {
    return this.parseResults?.meta.fields ?? []
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
        .filter(it => it !== undefined && it !== "occupations")
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

      const alignmentHolder: {[s: string]: string} = {}
      const jobcodes: string[] = []

      Object.keys(row).forEach(uploadedKey => {
        const fieldName = this.fieldMappings?.[uploadedKey]
        const value: string = row[uploadedKey].trim()
        if (fieldName !== undefined && value) {

          if (["author"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiNamedReference({name: value})
          }
          else if (["standards", "certifications", "employers"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiReferenceListUpdate(
              value.split(";").map(it => ApiNamedReference.fromString(it)).filter(it => it !== undefined).map(it => it as ApiNamedReference)
            )
          }
          else if (["keywords"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiStringListUpdate(value.split(";").map(it => it.trim()))
          } else if (fieldName === "occupations") {
            jobcodes.push(...value.split(";").map(it => it.trim()))
          } else if (fieldName === "alignmentUrl") {
            alignmentHolder.id = value.trim()
          } else if (fieldName === "alignmentName") {
            alignmentHolder.name = value.trim()
          } else {
            newSkill[fieldName] = value
          }
        }
      })

      if (jobcodes.length > 0) {
        newSkill.occupations = new ApiStringListUpdate(jobcodes)
      }

      if (alignmentHolder.id || alignmentHolder.name) {
        newSkill.alignments = new ApiReferenceListUpdate([new ApiNamedReference(alignmentHolder)])
      }

      return newSkill
    }).map((it: IRichSkillUpdate) => new ApiSkillUpdate(it))
    return skillUpdates
  }

  private initializeMapping(): void {
    const mappings: {[s: string]: string} = {}
    this.uploadedHeaders().forEach(it => {
      const fieldName = importSkillHeadersReverse[it.toLowerCase()]
      if (fieldName) {
        mappings[it] = fieldName
      }
    })

    this.handleMappingChanged(mappings)
  }

  private auditRecords(): void {
    this.previewSkills = this.skillsFromResults()

    this.searchingSimilarity = true
    const statements: string[] = this.previewSkills.map(it => it.skillStatement).map(it => it ?? "")
    this.richSkillService.similaritiesCheck(statements).subscribe(results => {
      this.searchingSimilarity = false
      this.similarSkills = results

      this.auditedSkills = this.previewSkills?.map((skill, idx) => {
        const required = ["skillName", "skillStatement"]
        // @ts-ignore
        const missing = required.filter(it => skill[it] === undefined)
        const similar = results[idx] ?? false
        return new AuditedImportSkill(skill, missing, similar)
      })
    })
  }

  private submitRecords(): void {
    if (this.auditedSkills === undefined) {
      return
    }

    this.importedSkills = this.auditedSkills?.filter(it => !it.isError && (this.importSimilarSkills || !it.similar))
    const skillUpdates = this.importedSkills.map(it => it.skill)

    this.showStepLoader()
    this.richSkillService.createSkills(skillUpdates).subscribe(results => {
      if (results) {
        this.hideStepLoader()
      }
    })

  }

  handleSimilarityOk(importSimilar: boolean): void {
    this.importSimilarSkills = importSimilar
  }
}

