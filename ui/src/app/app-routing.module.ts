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
  { path: "", redirectTo: "/skills", pathMatch: "full" },

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
  {path: "login", component: LoginComponent},  // redirect to oauth login
  {path: "logout", component: LogoutComponent},  // app logout
  {path: "login/success", component: LoginSuccessComponent},  // post-login redirect destination
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
