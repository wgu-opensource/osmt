import {Component, ElementRef, OnInit, ViewChild} from "@angular/core"
import {QuickLinksHelper} from "../../core/quick-links-helper"
import {ActivatedRoute, Router} from "@angular/router"
import {RichSkillService} from "../service/rich-skill.service"
import {ToastService} from "../../toast/toast.service"
import {Location} from "@angular/common"
import {Papa, ParseResult} from "ngx-papaparse"
import {ApiAlignment, ApiNamedReference, ApiSkill} from "../ApiSkill"
import {
  ApiAlignmentListUpdate,
  ApiReferenceListUpdate,
  ApiSkillUpdate,
  ApiStringListUpdate,
  IRichSkillUpdate
} from "../ApiSkillUpdate"
import {forkJoin, Observable} from "rxjs"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {Title} from "@angular/platform-browser";
import {AppConfig} from "../../app.config"
import { BatchImportOptionsEnum } from "./BatchImportOptionsEnum";
import { ApiSearch, ApiSkillListUpdate } from "../service/rich-skill-search.service";
import { CollectionService } from "../../collection/service/collection.service";
import { ApiSkillSummary } from "../ApiSkillSummary"


export enum ImportStep {
  UploadFile = 1,
  FieldMapping = 2,
  ReviewRecords = 3,
  Success = 4
}


export const importSkillHeaderOrder = [
  {field: "skillName", label: "RSD Name"},
  {field: "authors", label: "Authors"},
  {field: "skillStatement", label: "Skill Statement"},
  {field: "categories", label: "Categories"},
  {field: "keywords", label: "Keywords"},
  {field: "standards", label: "Standards"},
  {field: "certifications", label: "Certifications"},
  {field: "occupations", label: "Occupations"},
  {field: "employers", label: "Employers"},
]

export const importSkillTargetOptions = [
  {target: "existing", label: "Existing Collection"},
  {target: "new", label: "New Collection"},
  {target: "workspace", label: "User Workspace"},
]
export const allMappingHeaderOrder = (alignmentCount: number = 3): {field: string, label: string}[] => {
  const alignmentHeaders = [...Array(alignmentCount).keys()].map(i => {
    const label = (i > 0) ? ` ${i+1}` : ""
    return [
      {field: "alignmentName"+i, label: `Alignment${label} Name`},
      {field: "alignmentUrl"+i, label: `Alignment${label} URL`},
      {field: "alignmentFramework"+i, label: `Alignment${label} Framework`},
    ]
  }).flat()
  return importSkillHeaderOrder.concat(alignmentHeaders)
}


export const importSkillHeaders: {[p: string]: string} =
 importSkillHeaderOrder.reduce((acc: {[p: string]: string}, it) => {
   acc[it.field] = it.label
   return acc
 }, {})

export function importSkillHeadersReverse(alignmentCount: number=3): {[p: string]: string} {
  return allMappingHeaderOrder(alignmentCount).reduce((acc: { [p: string]: string }, it) => {
    acc[it.label.toLowerCase()] = it.field
    return acc
  }, {})
}

export class AuditedImportSkill {
  skill: ApiSkillUpdate
  missing: string[]
  similar: boolean
  similarities: any[] = []

  constructor(skill: ApiSkillUpdate, missing: string[], similar: boolean = false, similarities: any[]) {
    this.skill = skill
    this.missing = missing
    this.similar = similar
    this.similarities = similarities;
  }

  get nameMissing(): boolean {
    return !this.skill.skillName
  }

  get statementMissing(): boolean {
    return !this.skill.skillStatement
  }

  get authorMissing(): boolean {
    return !this.hasAuthors
  }

  get hasAuthors(): boolean {
    return this.skill.authors?.add !== undefined && this.skill.authors?.add.length > 0
  }

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

  @ViewChild("stepHeading") stepHeadingRef!: ElementRef

  currentStep: ImportStep = ImportStep.UploadFile

  stepLoaded?: Observable<ImportStep>

  uploading = false
  uploadedFile: any;
  uploadedFileError = false

  acceptableFileTypes = [
    "text/csv",
  ]
  parseResults?: ParseResult
  fieldMappings?: {[p: string]: string}
  previewSkills?: ApiSkillUpdate[]
  auditedSkills?: AuditedImportSkill[]
  importedSkills?: AuditedImportSkill[]
  skillsToBeImported?: ApiSkill[]

  alignmentCount: number = 3

  searchingSimilarity?: boolean
  similarSkills?: any[]
  importSimilarSkills = false

  docIcon = SvgHelper.path(SvgIcon.DOC)
  isHover: boolean = false
  target: string = ""

  get similarSkillCount(): number {
    return (this.similarSkills?.filter(it => it).length ?? 0)
  }

  constructor(protected router: Router,
              protected richSkillService: RichSkillService,
              protected toastService: ToastService,
              protected route: ActivatedRoute,
              protected location: Location,
              protected papa: Papa,
              protected titleService: Title,
              protected collectionService: CollectionService
  ) {
    super()
    this.resetState()
    this.route.queryParams.subscribe(params => this.target = params.to)
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Batch Import | ${this.whitelabel.toolName}`)
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
    this.focusAndScrollIntoView(this.stepHeadingRef.nativeElement)
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
    this.isHover = true
    $event.preventDefault()
    return false
  }
  handleFileLeave($event: DragEvent): boolean {
    this.isHover = false
    return true
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

      // if (this.acceptableFileTypes.indexOf(file.type) === -1) {
      //   this.uploadedFile = "The file you select must be CSV format."
      //   this.uploadedFileError = true
      //   return
      // }

      this.uploading = true
      this.papa.parse(file, {
        header: true,
        skipEmptyLines: true,
        complete: (results) => {
          this.uploading = false
          this.uploadedFileError = false
          this.parseResults = results
        },
        error: (error) => {
          this.uploadedFile = "There was an error reading the file you selected."
          this.uploadedFileError = true
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

  reverseMappings(): {[p: string]: string[]} {
    if (this.fieldMappings === undefined) {
      return {}
    }
    const reversed: {[p: string]: string[]} = {}
    Object.keys(this.fieldMappings).forEach(k => {
      const v: string = this.fieldMappings ? this.fieldMappings[k] : ""
      if (reversed[v] === undefined) {
        reversed[v] = []
      }
      reversed[v].push(k)
    })
    return reversed
  }

  skillsFromResults(): ApiSkillUpdate[] {
    const skillUpdates = this.parseResults?.data.map((row: { [x: string]: any; }) => {
      // tslint:disable-next-line:no-any
      const newSkill: {[s: string]: any} = {}

      const alignmentsHolder: ApiAlignment[] = []
      const jobcodes: string[] = []
      let hasAuthor = false
      var alignMatches

      Object.keys(row).forEach(uploadedKey => {
        const fieldName = this.fieldMappings?.[uploadedKey]
        const rawValue: any = row[uploadedKey]
        if (fieldName !== undefined && rawValue && typeof rawValue === "string") {
          const value: string = rawValue?.trim()

          if (["authors"].indexOf(fieldName) !== -1) {
            const authors = value.split(";").map(it => it.trim()).filter(it => it !== "")

            if (authors.length > 0) {
              newSkill[fieldName] = new ApiStringListUpdate(authors)
              hasAuthor = true
            }
          } else if (["categories"].indexOf(fieldName) !== -1) {
            const categories = value.split(";").map(it => it.trim()).filter(it => it !== "")

            if (categories.length > 0) {
              newSkill[fieldName] = new ApiStringListUpdate(categories)
            }
          }
          else if (["certifications", "employers"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiReferenceListUpdate(
              value.split(";").map(it => ApiNamedReference.fromString(it)).filter(it => it !== undefined).map(it => it as ApiNamedReference)
            )
          }
          else if (["keywords"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiStringListUpdate(value.split(";").map(it => it.trim()))
          } else if (fieldName === "occupations") {
            jobcodes.push(...value.split(";").map(it => it.trim()))
          } else if (alignMatches = fieldName.match(/alignment(Url|Name|Framework)(\d+)/)) {
            var alignIndex = Number(alignMatches[2])
            var alignField = alignMatches[1]

            if (!alignmentsHolder[alignIndex]) {
              alignmentsHolder[alignIndex] = new ApiAlignment({})
            }

            if (alignField === "Url") {
              alignmentsHolder[alignIndex].id = value
            }
            else if (alignField === "Name") {
              alignmentsHolder[alignIndex].skillName = value
            }
            else if (alignField === "Framework") {
              alignmentsHolder[alignIndex].isPartOf = new ApiNamedReference({name: value.trim()})
            }
          } else if (["standards"].indexOf(fieldName) !== -1) {
            newSkill[fieldName] = new ApiAlignmentListUpdate(
              value.split(";").map(it => ApiAlignment.fromString(it)).filter(it => it !== undefined).map(it => it as ApiAlignment)
            )
          } else {
            newSkill[fieldName] = value
          }
        }
      })

      if (jobcodes.length > 0) {
        newSkill.occupations = new ApiStringListUpdate(jobcodes)
      }

      if (alignmentsHolder.length > 0) {
        newSkill.alignments = new ApiAlignmentListUpdate(Array.from(alignmentsHolder))
      }

      if (!hasAuthor && AppConfig.settings.defaultAuthorValue && AppConfig.settings.defaultAuthorValue.length > 0) {
        newSkill["authors"] = new ApiStringListUpdate([AppConfig.settings.defaultAuthorValue])
      }

      return newSkill
    })
    let deduped = [...new Map(skillUpdates.map((item: { [x: string]: any }) =>[item["skillStatement"], item])).values()]
    let apiSkillUpdates = deduped.map(it => new ApiSkillUpdate(<IRichSkillUpdate>it))
    return apiSkillUpdates
  }

  private initializeMapping(): void {
    const mappings: {[s: string]: string} = {}
    this.uploadedHeaders().forEach(it => {
      const uploadedField = it.toLowerCase()
      const fieldName = importSkillHeadersReverse(this.alignmentCount)[uploadedField]
      if (fieldName) {
        mappings[it] = fieldName
      } else if (uploadedField === 'o*net job codes') {
        mappings[it] = 'occupations'
      }
    })

    this.handleMappingChanged(mappings)
  }

  private batchSimilarity(statements: string[]): Observable<Array<ApiSkillSummary[]>> {
    const chunk = (arr: string[], size: number) => {
      return Array.from({ length: Math.ceil(arr.length / size) }, (v, i) =>
        arr.slice(i * size, i * size + size)
      )
    }

    const chunks: string[][] = chunk(statements, 100)
    const observables: Observable<Array<ApiSkillSummary[]>>[] = chunks.map(it => this.richSkillService.similaritiesResults(it))
    return new Observable(observer => {
      forkJoin(observables).subscribe(it => {
        const allResponses: Array<ApiSkillSummary[]> = it.flat()
        observer.next(allResponses)
        observer.complete()
      }, err => observer.error(err))
    })
  }

  private auditRecords(): void {
    this.previewSkills = this.skillsFromResults()

    this.searchingSimilarity = true
    const statements: string[] = this.previewSkills.map(it => it.skillStatement).map(it => it ?? "")
    this.batchSimilarity(statements).subscribe(results => {
      this.searchingSimilarity = false
      this.similarSkills = results
      this.auditedSkills = this.previewSkills?.map((skill, idx) => {
        const required = ["skillName", "skillStatement"]
        // @ts-ignore
        const missing = required.filter(it => skill[it] === undefined)
        const similar = (results[idx]?.length ?? 0) > 0
        return new AuditedImportSkill(skill, missing, similar, results[idx])
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

    this.richSkillService.pollForTaskResult<ApiSkill[]>(
      this.richSkillService.createSkills(skillUpdates)
    ).subscribe(results => {
      if (results) {
        this.hideStepLoader()
        this.skillsToBeImported = results
      }
    })

  }

  handleSimilarityOk(importSimilar: boolean): void {
    this.importSimilarSkills = importSimilar
  }

  get validImportSkillsCount(): boolean {
    return this.skillsToBeImported ? this.skillsToBeImported?.length > 0 : false
  }

  protected handleClickAddToWorkspace(): void {
    let skillListUpdate = new ApiSkillListUpdate({
      add: new ApiSearch(
        {uuids:this.skillsToBeImported?.map(skill => skill.uuid)}
      )
    })
    this.toastService.showBlockingLoader()
    this.collectionService.getWorkspace().subscribe(workspace => {
      this.collectionService.updateSkillsWithResult(workspace.uuid, skillListUpdate, undefined).subscribe(result => {
        if (result) {
          const message = `You added ${result.modifiedCount} RSDs to your workspace.`
          this.toastService.showToast("Success!", message)
          this.toastService.hideBlockingLoader()
        }
      })
    })
  }

  protected handleAddToExistingCollection() {
    this.router.navigate(["/collections/add-skills"],
      {state:{selectedSkills: this.skillsToBeImported, totalCount:this.skillsToBeImported?.length}}
    )
  }

  protected handleAddToANewCollection() {
    this.router.navigate(["/collections/create/batch-import"],
      {state: {selectedSkills: this.skillsToBeImported, totalCount:this.skillsToBeImported?.length}}
    )
  }

  protected getBatchImportAction() {
    switch (this.target) {
      case BatchImportOptionsEnum.existing: {
        this.handleAddToExistingCollection()
        break
      }
      case BatchImportOptionsEnum.new: {
        this.handleAddToANewCollection()
        break
      }
      case BatchImportOptionsEnum.workspace: {
        this.handleClickAddToWorkspace()
        break
      }
      default: {
        break
      }
    }
  }

  getImportOptionButtonLabel(): string {
    switch (this.target) {
      case BatchImportOptionsEnum.existing: {
        return "Add to existing Collection"
      }
      case BatchImportOptionsEnum.new: {
        return "Add to a new Collection"
      }
      case BatchImportOptionsEnum.workspace: {
        return "Add to Workspace"
      }
      default: {
        return ""
      }
    }
  }

  protected updateTarget(destination: string) {
    this.target = destination
  }
}

