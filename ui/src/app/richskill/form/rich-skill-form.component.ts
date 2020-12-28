import {Component, Injectable, OnInit} from "@angular/core"
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms"
import {Location} from "@angular/common"
import {ActivatedRoute, ActivatedRouteSnapshot, CanDeactivate, Router, RouterStateSnapshot} from "@angular/router"
import {RichSkillService} from "../service/rich-skill.service"
import {Observable} from "rxjs"
import {ApiNamedReference, INamedReference, ApiSkill, KeywordType, IUuidReference} from "../ApiSkill"
import {ApiStringListUpdate, IStringListUpdate, ApiSkillUpdate, ApiReferenceListUpdate} from "../ApiSkillUpdate"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import { IJobCode } from "src/app/job-codes/Jobcode"
import {ToastService} from "../../toast/toast.service"
import {Title} from "@angular/platform-browser"
import {HasFormGroup} from "../../core/abstract-form.component"
import {notACopyValidator} from "../../validators/not-a-copy.validator"
import {ApiSkillSummary} from "../ApiSkillSummary";


@Component({
  selector: "app-rich-skill-form",
  templateUrl: "./rich-skill-form.component.html"
})
export class RichSkillFormComponent implements OnInit, HasFormGroup {
  skillForm = new FormGroup(this.getFormDefinitions())
  skillUuid: string | null = null
  existingSkill: ApiSkill | null = null

  skillLoaded: Observable<ApiSkill> | null = null
  skillSaved: Observable<ApiSkill> | null = null
  isDuplicating = false

  // Type ahead storage to append to the field on submit
  selectedStandards: string[] = []
  selectedJobCodes: string[] = []
  selectedKeywords: string[] = []
  selectedCertifications: string[] = []
  selectedEmployers: string[] = []

  // This allows this enum's constants to be used in the template
  keywordType = KeywordType

  // for skill statement similarity checking
  searchingSimilarity?: boolean
  similarSkills?: ApiSkillSummary[]

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private richSkillService: RichSkillService,
    private location: Location,
    private router: Router,
    private toastService: ToastService,
    private titleService: Title
  ) { }

  formGroup(): FormGroup { return this.skillForm }

  ngOnInit(): void {
    this.skillUuid = this.route.snapshot.paramMap.get("uuid")

    this.isDuplicating = window.location.pathname.endsWith("/duplicate")

    if (this.skillUuid) {
      this.skillLoaded = this.richSkillService.getSkillByUUID(this.skillUuid)
      this.skillLoaded.subscribe(skill => { this.setSkill(skill) })
    }

    this.titleService.setTitle(this.pageTitle())

    this.skillForm.controls.skillStatement.valueChanges.subscribe(newStatement => {
      if (this.searchingSimilarity !== undefined) {
        this.searchingSimilarity = undefined
      }
    })
  }

  pageTitle(): string {
    return `${this.existingSkill != null ? "Edit" : "Create"} Rich Skill Descriptor`
  }

  nameErrorMessage(): string {
    return this.skillForm.get("skillName")?.hasError("notACopy") ? "Name is still a copy" : "Name is required"
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      skillName: new FormControl("", Validators.compose([Validators.required, notACopyValidator])),
      skillStatement: new FormControl("", Validators.required),
      category: new FormControl(""),
      keywords: new FormControl(""),
      standards: new FormControl(""),
      collections: new FormControl(""),
      certifications: new FormControl(""),
      occupations: new FormControl(""),
      employers: new FormControl(""),
      alignmentText: new FormControl(""),
      alignmentUrl: new FormControl("", urlValidator),
    }
    if (AppConfig.settings.editableAuthor) {
      // @ts-ignore
      fields.author = new FormControl(AppConfig.settings.defaultAuthorValue, Validators.required)
    }
    return fields
  }

  diffUuidList(words: string[], collections?: IUuidReference[]): ApiStringListUpdate | undefined {
    const existing: Set<string> = new Set<string>(collections?.map(it => it.name))
    const provided = new Set(words)
    const removing = [...existing].filter(x => !provided.has(x))
    const adding = [...provided].filter(x => !existing.has(x))
    return (removing.length > 0 || adding.length > 0) ? new ApiStringListUpdate(adding, removing) : undefined
  }

  diffStringList(words: string[], keywords?: string[]): ApiStringListUpdate | undefined {
    const existing = new Set(keywords)
    const provided = new Set(words)
    const removing: string[] = [...existing].filter(x => !provided.has(x))
    const adding: string[] = [...provided].filter(x => !existing.has(x))
    return (removing.length > 0 || adding.length > 0) ? new ApiStringListUpdate(adding, removing) : undefined
  }

  diffReferenceList(words: string[], refs?: INamedReference[]): ApiReferenceListUpdate | undefined {
    const existing = new Set(refs?.map(it => this.stringFromNamedReference(it)))
    const provided = new Set(words)
    const removing = [...existing].filter(x => !provided.has(x)).map(it => this.namedReferenceForString(it))
      .filter(it => it).map(it => it as ApiNamedReference)
    const adding = [...provided].filter(x => !existing.has(x)).map(it => this.namedReferenceForString(it))
      .filter(it => it).map(it => it as ApiNamedReference)
    return (removing.length > 0 || adding.length > 0) ? new ApiReferenceListUpdate(adding, removing) : undefined
  }


  splitTextarea(textValue: string): Array<string> {
    return textValue.split(";").map(it => it.trim())
  }

  nonEmptyOrNull(s?: string): string | undefined {
    const val: string | undefined = s?.trim()
    if (val === undefined) { return undefined }
    return val?.length > 0 ? val : undefined
  }

  updateObject(): ApiSkillUpdate {
    const update = new ApiSkillUpdate({})
    const formValue = this.skillForm.value

    // pre-populate type-ahead values into field
    this.populateTypeAheadFieldsWithResults()

    const inputName = this.nonEmptyOrNull(formValue.skillName)
    if (inputName && (this.isDuplicating || this.existingSkill?.skillName !== inputName)) {
      update.skillName = inputName
    }

    const inputStatement = this.nonEmptyOrNull(formValue.skillStatement)
    if (inputStatement && (this.isDuplicating || this.existingSkill?.skillStatement !== inputStatement)) {
      update.skillStatement = inputStatement
    }

    if (AppConfig.settings.editableAuthor) {
      const author = ApiNamedReference.fromString(formValue.author)
      if (!this.existingSkill || this.isDuplicating || this.stringFromNamedReference(this.existingSkill.author) !== formValue.author) {
        update.author = author
      }
    }

    const inputCategory = this.nonEmptyOrNull(formValue.category)
    if (this.isDuplicating || this.existingSkill?.category !== inputCategory) {
      update.category = inputCategory ?? ""
    }

    const collectionsDiff = this.diffUuidList(formValue.collections, this.existingSkill?.collections)
    if (this.isDuplicating || collectionsDiff) { update.collections = collectionsDiff }

    const keywordDiff = this.diffStringList(this.splitTextarea(formValue.keywords), this.existingSkill?.keywords)
    if (this.isDuplicating || keywordDiff) { update.keywords = keywordDiff }

    const occupationsDiff = this.diffStringList(
      this.splitTextarea(formValue.occupations),
      this.existingSkill?.occupations?.map(it => this.stringFromJobCode(it))
    )
    if (this.isDuplicating || occupationsDiff) { update.occupations = occupationsDiff }

    // tslint:disable-next-line:variable-name
    const _handle_ref_list = (formFieldValue: string, fieldName: string, existing?: INamedReference[]) => {
      const diff = this.diffReferenceList(this.splitTextarea(formFieldValue), existing)
      if (this.isDuplicating || diff) { // @ts-ignore
        update[fieldName] = diff
      }
    }

    _handle_ref_list(formValue.standards, "standards", this.existingSkill?.standards)
    _handle_ref_list(formValue.certifications, "certifications", this.existingSkill?.certifications)
    _handle_ref_list(formValue.employers, "employers", this.existingSkill?.employers)


    // handle a single alignment with title and url only
    const firstAlignment: ApiNamedReference | undefined = this.existingSkill
      ?.alignments
      ?.map(it => new ApiNamedReference(it))
      ?.find(it => true)
    const inputAlignment = new ApiNamedReference({
      id: this.nonEmptyOrNull(formValue.alignmentUrl),
      name: this.nonEmptyOrNull(formValue.alignmentText)
    })

    if (this.isDuplicating || !firstAlignment?.equals(inputAlignment)) {
      const inputAlignmentDefined = (inputAlignment.id || inputAlignment.name)
      update.alignments = new ApiReferenceListUpdate(
        inputAlignmentDefined ? [inputAlignment] : undefined,
        firstAlignment ? [firstAlignment] : undefined
      )
    }


    return update
  }

  onSubmit(): void {
    const updateObject = this.updateObject()

    if (Object.keys(updateObject).length < 1) {
      return
    }

    if (this.skillUuid && !this.isDuplicating) {
      this.skillSaved = this.richSkillService.updateSkill(this.skillUuid, updateObject)
    } else {
      this.skillSaved = this.richSkillService.createSkill(updateObject)
    }

    if (this.skillSaved) {
      this.skillSaved.subscribe((result) => {
        this.skillForm.markAsPristine()
        const collectionCount = updateObject.collections?.add?.length ?? 0
        if (collectionCount > 0) {
          const collectionWord = collectionCount > 1 ? "collections" : "collection"
          this.toastService.showToast(
            `Removed from ${collectionWord}.`,
            `This skill was removed from ${collectionCount} ${collectionWord}.`
          )
        }
        this.router.navigate([`/skills/${result.uuid}/manage`])
      })
    }
  }

  namedReferenceForString(value: string): ApiNamedReference | undefined {
    const str = value.trim()
    if (str.length < 1) {
      return undefined
    } else if (str.indexOf("://") !== -1) {
      return new ApiNamedReference({id: str})
    } else {
      return new ApiNamedReference({name: str})
    }
  }

  stringFromNamedReference(ref?: INamedReference): string {
    return ref?.name ?? ref?.id ?? ""
  }

  stringFromJobCode(jobcode?: IJobCode): string {
    return jobcode?.code ?? ""
  }

  setSkill(skill: ApiSkill): void {
    this.existingSkill = skill

    const firstAlignment: INamedReference | undefined = skill.alignments?.find(it => true)

    const fields = {
      skillName: (this.isDuplicating ? "Copy of " : "") + skill.skillName,
      skillStatement: skill.skillStatement,
      category: skill.category ?? "",
      keywords: skill.keywords?.join("; ") ?? "",
      standards: skill.standards?.map(it => this.stringFromNamedReference(it)).join("; ") ?? "",
      collections: skill.collections?.map(it => it.name) ?? [],
      certifications: skill.certifications?.map(it => this.stringFromNamedReference(it)).join("; ") ?? "",
      occupations: skill.occupations?.map(it => this.stringFromJobCode(it)).join("; ") ?? "",
      employers: skill.employers?.map(it => this.stringFromNamedReference(it)).join("; ") ?? "",
      alignmentText: firstAlignment?.name ?? "",
      alignmentUrl: firstAlignment?.id ?? "",
    }
    if (AppConfig.settings.editableAuthor) {
      // @ts-ignore
      fields.author = this.stringFromNamedReference(skill.author)
    }
    this.skillForm.setValue(fields)


    if (skill.skillStatement) {
      this.checkForStatementSimilarity(skill.skillStatement)
    }
  }

  handleFormErrors(errors: unknown): void {
  }

  handleClickCancel(): boolean {
    this.location.back()
    return false
  }

  showAuthor(): boolean {
    return AppConfig.settings.editableAuthor
  }

  handleClickMissingFields(): boolean {
    const fieldOrder = [
      "skillName",
      "author",
      "skillStatement",
      "category",
      "keywords",
      "standards",
      "certifications",
      "occupations",
      "employers",
      "alignmentText",
      "alignmentUrl"
    ]
    this.skillForm.markAllAsTouched()
    for (const fieldName of fieldOrder) {
      const control = this.skillForm.controls[fieldName]
      if (control && !control.valid) {
        if (this.focusFormField(fieldName)) {
          return false
        }
      }
    }
    return false
  }

  focusFormField(fieldName: string): boolean {
    const containerId = `formfield-container-${fieldName}`
    const el = document.getElementById(containerId)
    if (el) {
      el.scrollIntoView()

      const fieldId = `formfield-${fieldName}`
      const field = document.getElementById(fieldId)
      if (field) {
        field.focus()
      }
      return true
    }
    return false
  }

  scrollToTop(): boolean {
    this.focusFormField("skillName")
    return false
  }

  populateTypeAheadFieldsWithResults(): void {
    const formValue = this.skillForm.value
    formValue.standards = [formValue.standards, ...this.selectedStandards].join("; ")
    formValue.occupations = [formValue.occupations, ...this.selectedJobCodes].join("; ")
    formValue.keywords = [formValue.keywords, ...this.selectedKeywords].join("; ")
    formValue.certifications = [formValue.certifications, ...this.selectedCertifications].join("; ")
    formValue.employers = [formValue.employers, ...this.selectedEmployers].join("; ")
  }

  handleStandardsTypeAheadResults(standards: string[]): void {
    this.selectedStandards = standards
  }

  handleJobCodesTypeAheadResults(jobCodes: string[]): void {
    this.selectedJobCodes = jobCodes
  }

  handleKeywordTypeAheadResults(keywords: string[]): void {
    this.selectedKeywords = keywords
  }

  handleCertificationTypeAheadResults(certifications: string[]): void {
    this.selectedCertifications = certifications
  }

  handleEmployersTypeAheadResults(employers: string[]): void {
    this.selectedEmployers = employers
  }

  handleStatementBlur($event: FocusEvent): void {
    const statement = this.skillForm.controls.skillStatement.value

    this.checkForStatementSimilarity(statement)
  }

  checkForStatementSimilarity(statement: string): void {
    this.searchingSimilarity = true
    this.richSkillService.similarityCheck(statement).subscribe(results => {
      this.similarSkills = results?.filter(s => this.existingSkill?.uuid !== s.uuid)
      this.searchingSimilarity = false
    })
  }

  get hasStatementWarning(): boolean {
    return (this.similarSkills?.length ?? -1) > 0
  }
}


