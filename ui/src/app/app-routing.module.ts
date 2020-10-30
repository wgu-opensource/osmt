import {NgModule} from "@angular/core"
import {Routes, RouterModule} from "@angular/router"
import {RichSkillPublicComponent} from "./richskill/detail/rich-skill-public/rich-skill-public.component"
import {RichSkillsLibraryComponent} from "./richskill/library/rich-skills-library.component"
import {RichSkillFormComponent, SkillFormDirtyGuard} from "./richskill/form/rich-skill-form.component"
import {LoginSuccessComponent} from "./auth/login-success.component"
import {LogoutComponent} from "./auth/logout.component"
import {AuthGuard} from "./auth/auth.guard"
import {LoginComponent} from "./auth/login.component"
import {RichSkillManageComponent} from "./richskill/detail/rich-skill-manage/rich-skill-manage.component"
import {RichSkillSearchResultsComponent} from "./search/rich-skill-search-results.component";
import {AdvancedSearchComponent} from "./search/advanced-search/advanced-search.component";

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
    component: RichSkillsLibraryComponent,
    canActivate: [AuthGuard],
  },

  // public views
  {path: "skills/:uuid", component: RichSkillPublicComponent},

  // Authed views
  {path: "skills/:uuid/manage", component: RichSkillManageComponent},

  // search
  {path: "search/skills", component: RichSkillSearchResultsComponent},
  {path: "search/advanced", component: AdvancedSearchComponent},

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
