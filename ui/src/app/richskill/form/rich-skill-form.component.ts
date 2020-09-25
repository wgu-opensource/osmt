import {Component, OnInit} from "@angular/core"
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {RichSkillService} from "../service/rich-skill.service";
import {Observable} from "rxjs";
import {RichSkill} from "../RichSkill";


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
  skillLoaded: Observable<RichSkill> | null = null
  existingSkill: RichSkill | null = null

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private richSkillService: RichSkillService
  ) {
  }

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

  onSubmit(): void {
    console.log("do the submit", this.skillForm.value)
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
}
