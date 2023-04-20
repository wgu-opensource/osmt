import {Component, Input} from "@angular/core"
import {ActivatedRoute, Router} from "@angular/router"
import {RichSkillService} from "../../../richskill/service/rich-skill.service"
import {ToastService} from "../../../toast/toast.service"
import {ApiCategory} from "../../ApiCategory"

@Component({
  selector: "app-category-detail-card",
  templateUrl: "./category-detail-card.component.html"
})
export class CategoryDetailCardComponent {

  @Input() category: ApiCategory | undefined
  @Input() indexOfFirstSkill: number | undefined = undefined
  @Input() currentOnPage: number | undefined = undefined
  @Input() showSkillCount: boolean = true

  constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService
  ) {
  }

  get categoryName(): string {
    return this.category?.name ?? ""
  }

  get categorySkillsLabel(): string {
    let label = (this.showSkillCount && this.categorySkillCount) ?
      `${this.categorySkillCount} RSD${(this.category?.skillCount != 1) ? 's' : ''} with category.` : ""

    label = (this.firstSkillIndex && this.lastSkillIndex) ?
      `${label} Viewing ${this.firstSkillIndex}-${this.lastSkillIndex}.` : label

    return label
  }

  get categorySkillCount(): string | undefined {
    return (this.category?.skillCount) ? this.category.skillCount.toString() : undefined
  }

  get firstSkillIndex(): string | undefined {
    return (this.indexOfFirstSkill) ? `${this.indexOfFirstSkill + 1}` : undefined
  }

  get lastSkillIndex(): string | undefined  {
    return (this.indexOfFirstSkill && this.currentOnPage) ? `${this.indexOfFirstSkill + this.currentOnPage}` : undefined
  }
}
