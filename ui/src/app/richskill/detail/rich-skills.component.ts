import {Component, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {RichSkill} from "../RichSkill"

@Component({
  selector: "app-rich-skills",
  templateUrl: "./rich-skills.component.html",
  styleUrls: ["./rich-skills.component.css"]
})
export class RichSkillsComponent implements OnInit {

  skills: RichSkill[] = []

  constructor(private richSkillService: RichSkillService) {
  }

  ngOnInit(): void {
    this.getSkills()
  }

  getSkills(): void {
    this.richSkillService.getSkills().subscribe(skills => this.skills = skills)
  }

}
