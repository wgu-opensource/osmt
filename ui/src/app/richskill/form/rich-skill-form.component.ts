import {Component, Injectable, OnInit} from "@angular/core"
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Location} from "@angular/common";
import {ActivatedRoute, ActivatedRouteSnapshot, CanDeactivate, Router, RouterStateSnapshot} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {Observable} from "rxjs";
import {ApiNamedReference, INamedReference, ApiSkill} from "../ApiSkill";
import {ApiStringListUpdate, IStringListUpdate, ApiSkillUpdate, ApiReferenceListUpdate} from "../ApiSkillUpdate";
import {AppConfig} from "../../app.config";
import {urlValidator} from "../../validators/url.validator";
import { IJobCode } from 'src/app/job-codes/Jobcode';
import {ToastService} from "../../toast/toast.service";


@Component({
  selector: "app-rich-skill-form",
  templateUrl: "./rich-skill-form.component.html"
})
export class RichSkillFormComponent implements OnInit {
  skillForm = new FormGroup(this.getFormDefinitions())
  skillUuid: string | null = null
  existingSkill: ApiSkill | null = null

  skillLoaded: Observable<ApiSkill> | null = null
  skillSaved: Observable<ApiSkill> | null = null

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private richSkillService: RichSkillService,
    private location: Location,
    private router: Router,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.skillUuid = this.route.snapshot.paramMap.get("uuid")

    if (this.skillUuid) {
      this.skillLoaded = this.richSkillService.getSkillByUUID(this.skillUuid)
      this.skillLoaded.subscribe(skill => { this.setSkill(skill) })
    }
  }

  pageTitle(): string {
    return `${this.existingSkill != null ? "Edit" : "Create"} Rich Skill Descriptor`
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      skillName: new FormControl("", Validators.required),
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
      .filter(it => it).map(it => it as INamedReference)
    const adding = [...provided].filter(x => !existing.has(x)).map(it => this.namedReferenceForString(it))
      .filter(it => it).map(it => it as INamedReference)
    return (removing.length > 0 || adding.length > 0) ? new ApiReferenceListUpdate(adding, removing) : undefined

  }


  splitTextarea(textValue: string): Array<string> {
    return textValue.split(";").map(it => it.trim())
  }

  parseAuthor(textValue: string): ApiNamedReference | undefined {
    const val: string = textValue.trim()
    if (val.length < 1) {
      return undefined
    }

    if (val.indexOf("://") !== -1) {
      return new ApiNamedReference({id: val})
    } else {
      return new ApiNamedReference({name: val})
    }
  }

  nonEmptyOrNull(s?: string): string | undefined {
    const val: string | undefined = s?.trim()
    if (val === undefined) { return undefined }
    return val?.length > 0 ? val : undefined
  }

  updateObject(): ApiSkillUpdate {
    const update = new ApiSkillUpdate()
    const formValue = this.skillForm.value

    const inputName = this.nonEmptyOrNull(formValue.skillName)
    if (inputName && this.existingSkill?.skillName !== inputName) {
      update.skillName = inputName
    }

    const inputStatement = this.nonEmptyOrNull(formValue.skillStatement)
    if (inputStatement && this.existingSkill?.skillStatement !== inputStatement) {
      update.skillStatement = inputStatement
    }

    if (AppConfig.settings.editableAuthor) {
      const author = this.parseAuthor(formValue.author)
      if (!this.existingSkill || this.stringFromNamedReference(this.existingSkill.author) !== formValue.author) {
          update.author = author
      }
    }

    const inputCategory = this.nonEmptyOrNull(formValue.category)
    if (this.existingSkill?.category !== inputCategory) {
      update.category = inputCategory ?? ""
    }

    const collectionsDiff = this.diffStringList(formValue.collections, this.existingSkill?.collections)
    if (collectionsDiff) { update.collections = collectionsDiff }

    const keywordDiff = this.diffStringList(this.splitTextarea(formValue.keywords), this.existingSkill?.keywords)
    if (keywordDiff) { update.keywords = keywordDiff }

    const occupationsDiff = this.diffStringList(
      this.splitTextarea(formValue.occupations),
      this.existingSkill?.occupations?.map(it => this.stringFromJobCode(it))
    )
    if (occupationsDiff) { update.occupations = occupationsDiff }

    // tslint:disable-next-line:variable-name
    const _handle_ref_list = (formFieldValue: string, fieldName: string, existing?: INamedReference[]) => {
      const diff = this.diffReferenceList(this.splitTextarea(formFieldValue), existing)
      if (diff) { // @ts-ignore
        update[fieldName] = diff
      }
    }
    _handle_ref_list(formValue.standards, "standards", this.existingSkill?.standards)
    _handle_ref_list(formValue.certifications, "certifications", this.existingSkill?.certifications)
    _handle_ref_list(formValue.employers, "employers", this.existingSkill?.employers)


    // handle a single alignment with title and url only
    const firstAlignment: INamedReference | undefined = this.existingSkill?.alignments?.find(it => true)
    const inputAlignment = new ApiNamedReference({
      id: this.nonEmptyOrNull(formValue.alignmentUrl),
      name: this.nonEmptyOrNull(formValue.alignmentText)
    })
    if ((inputAlignment.id || inputAlignment.name) && firstAlignment !== inputAlignment) {
      update.alignments = new ApiReferenceListUpdate(
        [inputAlignment],
        firstAlignment !== undefined ? [firstAlignment as INamedReference] : []
      )
    }


    return update
  }

  onSubmit(): void {
    const updateObject = this.updateObject()
    console.log("do the submit", this.skillForm.value, updateObject)

    if (Object.keys(updateObject).length < 1) {
      console.log("no changes to submit")
      return
    }

    if (this.skillUuid) {
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

  namedReferenceForString(value: string): INamedReference | undefined {
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
    return jobcode?.name ?? jobcode?.code ?? ""
  }

  setSkill(skill: ApiSkill): void {
    this.existingSkill = skill

    const firstAlignment: INamedReference | undefined = skill.alignments?.find(it => true)

    const fields = {
      skillName: skill.skillName,
      skillStatement: skill.skillStatement,
      category: skill.category ?? "",
      keywords: skill.keywords?.join("; ") ?? "",
      standards: skill.standards?.map(it => this.stringFromNamedReference(it)).join("; ") ?? "",
      collections: skill.collections?.slice() ?? [],
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
  }

  handleFormErrors(errors: unknown): void {
    console.log("component got errors", errors)
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
}


@Injectable()
export class SkillFormDirtyGuard implements CanDeactivate<RichSkillFormComponent> {

  canDeactivate(
    component: RichSkillFormComponent,
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean {
    if (!component.skillForm.pristine) {
      return new Observable((observer) => {
        observer.next(confirm("Whoa, there! You have unsaved changes.\nIf you leave this page without saving, you'll lose your edits. Are you sure you want to leave?"))
        observer.complete()
      })
    }
    return true
  }
}
