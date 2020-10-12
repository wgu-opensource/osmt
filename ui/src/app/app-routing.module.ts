import {NgModule} from "@angular/core"
import {Routes, RouterModule} from "@angular/router"
import {RichSkillComponent} from "./richskill/detail/rich-skill.component"
import {RichSkillsComponent} from "./richskill/detail/rich-skills.component"
import {RichSkillFormComponent, SkillFormDirtyGuard} from "./richskill/form/rich-skill-form.component";
import {LoginSuccessComponent} from "./auth/login-success.component";
import {LogoutComponent} from "./auth/logout.component";
import {AuthGuard} from "./auth/auth.guard";
import {LoginComponent} from "./auth/login.component";

const routes: Routes = [
  // {path: "api/skills/:uuid", component: RichSkillComponent},

  // create skill
  {path: "skills/create",
    component: RichSkillFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [SkillFormDirtyGuard]},

  // edit skill
  {path: "skills/:uuid/edit",
    component: RichSkillFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [SkillFormDirtyGuard]
  },

  // skills library
  {path: "skills",
    component: RichSkillsComponent,
    canActivate: [AuthGuard],
  },

  // public views
  {path: "skills/:uuid", component: RichSkillComponent},

  // authentication redirects
  {path: "login", component: LoginComponent},  // redirect to login
  {path: "logout", component: LogoutComponent},  // post-login redirect destination
  {path: "login/success", component: LoginSuccessComponent},  // login redirect destination
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
