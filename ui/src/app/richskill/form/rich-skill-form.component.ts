import {Component, OnInit} from "@angular/core"
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {Observable} from "rxjs";
import {RichSkill} from "../RichSkill";
import {RichSkillUpdate} from "../RichSkillUpdate";


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
  })
  skillUuid: string | null = null
  existingSkill: RichSkill | null = null

  skillLoaded: Observable<RichSkill> | null = null
  skillSaved: Observable<RichSkill> | null = null

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

  updateObject(): RichSkillUpdate {
    const update = new RichSkillUpdate({})
    const formValue = this.skillForm.value

    if (!this.existingSkill || this.existingSkill.name != formValue.skillName) {
      update.skillName = formValue.skillName
    }
    if (!this.existingSkill || this.existingSkill.statement != formValue.skillStatement) {
      update.skillStatement = formValue.skillStatement
    }

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

  setSkill(skill: RichSkill): void {
    console.log("retrieved skill", skill)
    this.existingSkill = skill
    this.skillForm.setValue({
      skillName: skill.name,
      author: skill.author?.name ? skill.author.name : "",
      skillStatement: skill.statement,
      category: skill.category?.name ? skill.category.name : "",
      keywords: ""
    })
  }

  handleFormErrors(errors: any): void {
    console.log("component got errors", errors)
  }
}
