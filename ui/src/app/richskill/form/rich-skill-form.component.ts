import {Component, OnInit} from "@angular/core"
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {Observable} from "rxjs";
import {ApiNamedReference, INamedReference, ApiSkill} from "../ApiSkill";
import {ApiStringListUpdate, IStringListUpdate, ApiSkillUpdate, ApiReferenceListUpdate} from "../ApiSkillUpdate";


@Component({
  selector: "app-rich-skill-form",
  templateUrl: "./rich-skill-form.component.html"
})
export class RichSkillFormComponent implements OnInit {
  skillForm = new FormGroup({
    skillName: new FormControl("", Validators.required),
    author: new FormControl("", Validators.required),
    skillStatement: new FormControl("", Validators.required),
    category: new FormControl(""),
    keywords: new FormControl(""),
    standards: new FormControl(""),
  })
  skillUuid: string | null = null
  existingSkill: ApiSkill | null = null

  skillLoaded: Observable<ApiSkill> | null = null
  skillSaved: Observable<ApiSkill> | null = null

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private richSkillService: RichSkillService) { }

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
    console.log("diffReferenceList", existing, provided, removing, adding)
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

  updateObject(): ApiSkillUpdate {
    const update = new ApiSkillUpdate()
    const formValue = this.skillForm.value

    if (!this.existingSkill || this.existingSkill.skillName !== formValue.skillName) {
      update.skillName = formValue.skillName
    }

    if (!this.existingSkill || this.existingSkill.skillStatement !== formValue.skillStatement) {
      update.skillStatement = formValue.skillStatement
    }

    const author = this.parseAuthor(formValue.author)
    if (author) {
      if (!this.existingSkill || this.stringFromNamedReference(this.existingSkill.author) !== formValue.author) {
        update.author = author
      }
    }

    if (!this.existingSkill || this.existingSkill.category !== formValue.category) {
      update.category = formValue.category
    }

    const keywordDiff = this.diffStringList(this.splitTextarea(formValue.keywords), this.existingSkill?.keywords)
    if (keywordDiff) { update.keywords = keywordDiff }

    const standardsDiff = this.diffReferenceList(this.splitTextarea(formValue.standards), this.existingSkill?.standards)
    if (standardsDiff) { update.standards = standardsDiff }

    return update
  }

  onSubmit(): void {
    const updateObject = this.updateObject()
    console.log("do the submit", this.skillForm.value, Object.keys(updateObject))

    if (Object.keys(updateObject).length < 1) {
      console.log("no changes to submit")
      return
    }

    if (this.skillUuid) {
      console.log("Updating record", updateObject)
      this.skillSaved = this.richSkillService.updateSkill(this.skillUuid, updateObject)
    } else {
      console.log("creating record", updateObject)
      this.skillSaved = this.richSkillService.createSkill(updateObject)
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
    return (ref?.name ? ref?.name : ref?.id ? ref?.id : "")
  }

  setSkill(skill: ApiSkill): void {
    console.log("retrieved skill", skill)
    this.existingSkill = skill
    this.skillForm.setValue({
      skillName: skill.skillName,
      author: this.stringFromNamedReference(skill.author),
      skillStatement: skill.skillStatement,
      category: skill.category ?? "",
      keywords: skill.keywords?.join("; ") ?? "",
      standards: skill.standards?.map(it => this.stringFromNamedReference(it)).join("; ") ?? ""
    })
  }

  handleFormErrors(errors: unknown): void {
    console.log("component got errors", errors)
  }
}
