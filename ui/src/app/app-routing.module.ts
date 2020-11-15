import {NgModule} from "@angular/core"
import {RouterModule, Routes} from "@angular/router"
import {RichSkillPublicComponent} from "./richskill/detail/rich-skill-public/rich-skill-public.component"
import {RichSkillsLibraryComponent} from "./richskill/library/rich-skills-library.component"
import {RichSkillFormComponent} from "./richskill/form/rich-skill-form.component"
import {LoginSuccessComponent} from "./auth/login-success.component"
import {LogoutComponent} from "./auth/logout.component"
import {AuthGuard} from "./auth/auth.guard"
import {LoginComponent} from "./auth/login.component"
import {RichSkillManageComponent} from "./richskill/detail/rich-skill-manage/rich-skill-manage.component"
import {RichSkillSearchResultsComponent} from "./search/rich-skill-search-results.component";
import {AdvancedSearchComponent} from "./search/advanced-search/advanced-search.component";
import {AddSkillsCollectionComponent} from "./collection/add-skills-collection.component";
import {CollectionFormComponent} from "./collection/create-collection/collection-form.component"
import {FormDirtyGuard} from "./core/abstract-form.component";
import {CollectionsLibraryComponent} from "./table/collections-library.component";
import {CollectionSearchResultsComponent} from "./collection/collection-search-results.component";

const routes: Routes = [
  { path: "", redirectTo: "/skills", pathMatch: "full" },

  /* SKILLS */

  // create skill
  {path: "skills/create",
    component: RichSkillFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [FormDirtyGuard]
  },
  // skill search results
  {path: "skills/search",
    component: RichSkillSearchResultsComponent,
    canActivate: [AuthGuard],
  },
  // edit skill
  {path: "skills/:uuid/edit",
    component: RichSkillFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [FormDirtyGuard]
  },
  // manage skill
  {path: "skills/:uuid/manage",
    component: RichSkillManageComponent,
    canActivate: [AuthGuard]
  },
  // skills library
  {path: "skills",
    component: RichSkillsLibraryComponent,
    canActivate: [AuthGuard],
  },

  /* COLLECTIONS */

  // create collection
  {path: "collections/create",
    component: CollectionFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [FormDirtyGuard]
  },
  // collection search results
  {path: "collections/search",
    component: CollectionSearchResultsComponent,
    canActivate: [AuthGuard],
  },
  // edit collection
  {path: "collections/:uuid/edit",
    component: CollectionFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [FormDirtyGuard]
  },
  // manage collection
  // {path: "collections/:uuid/manage",
  //   component: CollectionManageComponent,
  //   canActivate: [AuthGuard]
  // },
  // add skills to a collection
  {path: "collections/add-skills",
    component: AddSkillsCollectionComponent,
    canActivate: [AuthGuard],
  },
  // collections library
  {path: "collections",
    component: CollectionsLibraryComponent,
    canActivate: [AuthGuard],
  },

  // advanced search
  {path: "search",
    component: AdvancedSearchComponent,
    canActivate: [AuthGuard],
  },

  /* PUBLIC VIEWS */
  {path: "skills/:uuid", component: RichSkillPublicComponent},
  // {path: "collections/:uuid", component: CollectionPublicComponent},

  /* AUTHENTICATION REDIRECTS */
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
