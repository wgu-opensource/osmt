import {Component, Injectable, OnInit} from "@angular/core"
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidationErrors, Validators} from "@angular/forms"
import {Location} from "@angular/common"
import {ActivatedRoute, ActivatedRouteSnapshot, CanDeactivate, Router, RouterStateSnapshot} from "@angular/router"
import {RichSkillService} from "../service/rich-skill.service"
import {Observable} from "rxjs"
import {
  ApiNamedReference,
  INamedReference,
  ApiSkill,
  KeywordType,
  IUuidReference,
  ApiAlignment,
  IRef, IAlignment
} from "../ApiSkill"
import {
  ApiStringListUpdate,
  IStringListUpdate,
  ApiSkillUpdate,
  ApiReferenceListUpdate,
  ApiAlignmentListUpdate
} from "../ApiSkillUpdate"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import { IJobCode } from "src/app/job-codes/Jobcode"
import {ToastService} from "../../toast/toast.service"
import {Title} from "@angular/platform-browser"
import {HasFormGroup} from "../../core/abstract-form.component"
import {notACopyValidator} from "../../validators/not-a-copy.validator"
import {ApiSkillSummary} from "../ApiSkillSummary"
import {Whitelabelled} from "../../../whitelabel"


@Component({
  selector: "app-rich-skill-form",
  templateUrl: "./rich-skill-form.component.html"
})
export class RichSkillFormComponent extends Whitelabelled implements OnInit, HasFormGroup  {
  skillForm = new FormGroup(this.getFormDefinitions())
  alignmentForms: FormGroup[] = []
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
  ) {
    super()
  }

  formGroup(): FormGroup { return this.skillForm }

  ngOnInit(): void {
    this.skillUuid = this.route.snapshot.paramMap.get("uuid")

    this.isDuplicating = window.location.pathname.endsWith("/duplicate")
    if (this.isDuplicating) {
      // If we're copying a skill, immediately check validation of the name so the user can make it unique
      this.formGroup().controls.skillName.markAsTouched()
    }

    if (this.skillUuid) {
      this.skillLoaded = this.richSkillService.getSkillByUUID(this.skillUuid)
      this.skillLoaded.subscribe(skill => { this.setSkill(skill) })
    }

    this.titleService.setTitle(`${this.pageTitle()} | ${this.whitelabel.toolName}`)

    this.skillForm.controls.skillStatement.valueChanges.subscribe(newStatement => {
      if (this.searchingSimilarity !== undefined) {
        this.searchingSimilarity = undefined
      }
    })
  }

  pageTitle(): string {
    if (this.isDuplicating) { return "Edit Copy of RSD" }
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
    const existing: Set<string> = new Set(refs?.map(it => ApiNamedReference.formatRef(it)).filter(it => it) ?? [])
    const provided: Set<string> = new Set(words.filter(it => it))
    const removing = [...existing].filter(x => !provided.has(x)).map(it => ApiNamedReference.fromString(it))
      .filter(it => it).map(it => it as ApiNamedReference) // filterNotNull
    const adding = [...provided].filter(x => !existing.has(x)).map(it => ApiNamedReference.fromString(it))
      .filter(it => it).map(it => it as ApiNamedReference) // filterNotNull
    return (removing.length > 0 || adding.length > 0) ? new ApiReferenceListUpdate(adding, removing) : undefined
  }

  diffAlignmentList(provided: ApiAlignment[], refs?: IAlignment[]): ApiAlignmentListUpdate | undefined {
    const existing: ApiAlignment[] = refs?.map(it => new ApiAlignment(it)) ?? []
    const removing: ApiAlignment[] = [...existing].filter(x => provided.findIndex(y => x.equals(y)) === -1)
    const adding: ApiAlignment[] = [...provided].filter(x => existing.findIndex(y => x.equals(y)) === -1)
    return (removing.length > 0 || adding.length > 0) ? new ApiAlignmentListUpdate(adding, removing) : undefined
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

    const standards = this.splitTextarea(formValue.standards).map(s => new ApiAlignment({skillName: s}))

    const standardsDiff = this.diffAlignmentList(standards, this.existingSkill?.standards)
    if (this.isDuplicating || standardsDiff) {
      update.standards = standardsDiff
    }

    const certificationsDiff = this.diffReferenceList(this.splitTextarea(formValue.certifications), this.existingSkill?.certifications)
    if (this.isDuplicating || certificationsDiff) {
      update.certifications = certificationsDiff
    }

    const employersDiff = this.diffReferenceList(this.splitTextarea(formValue.employers), this.existingSkill?.employers)
    if (this.isDuplicating || employersDiff) {
      update.employers = employersDiff
    }

    const alignments = this.alignmentForms.map(group => {
      const groupData = group.value
      const frameworkName = this.nonEmptyOrNull(groupData.alignmentFramework)
      return new ApiAlignment({
        id: this.nonEmptyOrNull(groupData.alignmentUrl),
        skillName: this.nonEmptyOrNull(groupData.alignmentText),
        isPartOf: frameworkName ? new ApiNamedReference({name: frameworkName}) : undefined
      })
    }).filter(a => a.id || a.skillName)
    const alignmentDiff = this.diffAlignmentList(alignments, this.existingSkill?.alignments)
    if (this.isDuplicating || alignmentDiff) {
      update.alignments = alignmentDiff
    }


    return update
  }

  get formValid(): boolean {
    const alignments_valid = !this.alignmentForms.map(group => group.valid).some(x => !x)
    return alignments_valid && this.skillForm.valid
  }

  get formDirty(): boolean {
    const alignments_dirty = this.alignmentForms.map(x => x.dirty).some(x => x)
    return alignments_dirty || this.skillForm.dirty
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
        if (!result) { return }

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

  stringFromAlignment(ref?: IAlignment): string {
    return ref?.skillName ?? ref?.id ?? ""
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

    if (!this.isDuplicating) {
      this.titleService.setTitle(`${skill.skillName} | ${this.pageTitle()} | ${this.whitelabel.toolName}`)
    }

    const firstAlignment: IAlignment | undefined = skill.alignments?.find(it => true)

    const fields = {
      skillName: (this.isDuplicating ? "Copy of " : "") + skill.skillName,
      skillStatement: skill.skillStatement,
      category: skill.category ?? "",
      keywords: skill.keywords?.join("; ") ?? "",
      standards: skill.standards?.map(it => this.stringFromAlignment(it)).join("; ") ?? "",
      collections: skill.collections?.map(it => it.name) ?? [],
      certifications: skill.certifications?.map(it => this.stringFromNamedReference(it)).join("; ") ?? "",
      occupations: skill.occupations?.map(it => this.stringFromJobCode(it)).join("; ") ?? "",
      employers: skill.employers?.map(it => this.stringFromNamedReference(it)).join("; ") ?? ""
    }
    if (AppConfig.settings.editableAuthor) {
      // @ts-ignore
      fields.author = this.stringFromNamedReference(skill.author)
    }
    this.skillForm.setValue(fields)

    skill.alignments.forEach(a => this.addAlignment(a))

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
    formValue.standards = this.selectedStandards.join("; ")
    formValue.occupations = this.selectedJobCodes.join("; ")
    formValue.keywords = this.selectedKeywords.join("; ")
    formValue.certifications = this.selectedCertifications.join("; ")
    formValue.employers = this.selectedEmployers.join("; ")
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
      this.similarSkills = this.isDuplicating ? results : results?.filter(s => this.existingSkill?.uuid !== s.uuid)
      this.searchingSimilarity = false
    })
  }

  get hasStatementWarning(): boolean {
    return (this.similarSkills?.length ?? -1) > 0
  }

  addAlignment(existing?: IAlignment): boolean {

    const fields = {
      alignmentText: new FormControl("", Validators.required),
      alignmentUrl: new FormControl("", Validators.compose([Validators.required, urlValidator])),
      alignmentFramework: new FormControl(""),
    }
    const group = new FormGroup(fields)

    if (existing) {
      group.setValue({
        alignmentText: existing.skillName ?? "",
        alignmentUrl: existing.id ?? "",
        alignmentFramework: existing.isPartOf?.name ?? "",
      })
    }
    this.alignmentForms.push(group)

    return false
  }
  removeAlignment(alignmentIndex: number): boolean {
    this.alignmentForms.splice(alignmentIndex, 1)

    return false
  }
}


