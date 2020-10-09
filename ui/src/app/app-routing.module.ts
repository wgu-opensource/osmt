import {NgModule} from "@angular/core"
import {Routes, RouterModule} from "@angular/router"
import {RichSkillComponent} from "./richskill/detail/rich-skill.component"
import {RichSkillsComponent} from "./richskill/detail/rich-skills.component"
import {RichSkillFormComponent, SkillFormDirtyGuard} from "./richskill/form/rich-skill-form.component";

const routes: Routes = [
  {path: "api/skills/:uuid", component: RichSkillComponent},

  {path: "skills/create", component: RichSkillFormComponent, canDeactivate: [SkillFormDirtyGuard]},  // create skill
  {path: "skills/:uuid", component: RichSkillComponent},  // public skill view
  {path: "skills/:uuid/edit", component: RichSkillFormComponent, canDeactivate: [SkillFormDirtyGuard]},  // edit skill

  {path: "skills", component: RichSkillsComponent},  // skills library
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
