import {Component, OnInit} from "@angular/core"
import {RichSkill} from "../RichSkill"
import {RichSkillService} from "../service/rich-skill.service"
import {ActivatedRoute, ParamMap} from "@angular/router"

@Component({
  selector: "app-richskill",
  templateUrl: "./rich-skill.component.html",
  styleUrls: ["./rich-skill.component.css"]
})
export class RichSkillComponent implements OnInit {

  uuidParam: string | null
  richSkill: RichSkill | null = null
  loading = true

  constructor(private richSkillService: RichSkillService, private route: ActivatedRoute) {
    this.route.params.subscribe(params => console.log(params))
    console.log(this.route.snapshot.paramMap.get("uuid"))
    this.uuidParam = this.route.snapshot.paramMap.get("uuid")
  }

  ngOnInit(): void {
    if (this.uuidParam !== null) {
      this.getSkill(this.uuidParam)
    }
  }

  getSkill(uuid: string): void {
    this.richSkillService.getSkillByUUID(uuid).subscribe(
      skill => {
        this.richSkill = skill
        this.loading = false
      },
      (error) => {
        console.log(`Error loading skill: ${error}`)
        this.loading = false
      }
    )
  }
}

