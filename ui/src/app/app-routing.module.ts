import {NgModule} from "@angular/core"
import {Routes, RouterModule} from "@angular/router"
import {RichSkillPublicComponent} from "./richskill/detail/rich-skill-public/rich-skill-public.component"
import {RichSkillsComponent} from "./richskill/list/rich-skills.component"
import {RichSkillFormComponent, SkillFormDirtyGuard} from "./richskill/form/rich-skill-form.component";
import {RichSkillManageComponent} from "./richskill/detail/rich-skill-manage/rich-skill-manage.component";

const routes: Routes = [
  {path: "api/skills/:uuid", component: RichSkillPublicComponent},

  {path: "skills/create", component: RichSkillFormComponent, canDeactivate: [SkillFormDirtyGuard]},  // create skill
  {path: "skills/:uuid", component: RichSkillPublicComponent},  // public skill view
  {path: "skills/:uuid/edit", component: RichSkillFormComponent, canDeactivate: [SkillFormDirtyGuard]},  // edit skill
  {path: "skills/:uuid/manage", component: RichSkillManageComponent},

  {path: "skills", component: RichSkillsComponent},  // skills library
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
