import {NgModule} from "@angular/core"
import {Routes, RouterModule} from "@angular/router"
import {RichSkillComponent} from "./richskill/detail/rich-skill.component"
import {RichSkillsComponent} from "./richskill/detail/rich-skills.component"

const routes: Routes = [
  {
    path: "skills/:uuid",
    component: RichSkillComponent
  },
  {
    path: "skills",
    component: RichSkillsComponent
  }
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
