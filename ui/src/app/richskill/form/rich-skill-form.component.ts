import {Component, OnInit} from "@angular/core"
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {FormFieldText} from "../../form/form-field-text.component"


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

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
  }

  onSubmit(): void {
    console.log("do the submit", this.skillForm.value)
  }
}
