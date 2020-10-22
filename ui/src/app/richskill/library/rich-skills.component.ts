import {Component, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {ApiSkill} from "../ApiSkill"
import {Router} from "@angular/router";
import {AuthService} from "../../auth/auth-service";

@Component({
  selector: "app-rich-skills",
  templateUrl: "./rich-skills.component.html"
})
export class RichSkillsComponent implements OnInit {

  skills: ApiSkill[] = []
  loading = true

  constructor(private richSkillService: RichSkillService, private router: Router, private authService: AuthService) {
  }

  ngOnInit(): void {
    this.getSkills()
  }

  getSkills(): void {
    this.richSkillService.getSkills(999)
      .subscribe(
        skills => {
          this.skills = skills
          this.loading = false
        },
        (error) => {
          console.log(`Error loading skill: ${error}`)
          this.loading = false
          this.authService.logout()
          this.router.navigate(["/login"])
        }
      )
  }

}
