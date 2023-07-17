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
import {RichSkillSearchResultsComponent} from "./search/rich-skill-search-results.component"
import {AdvancedSearchComponent} from "./search/advanced-search/advanced-search.component"
import {AddSkillsCollectionComponent} from "./collection/add-skills-collection.component"
import {CollectionFormComponent} from "./collection/create-collection/collection-form.component"
import {FormDirtyGuard} from "./core/abstract-form.component"
import {CollectionsLibraryComponent} from "./table/collections-library.component"
import {CollectionSearchResultsComponent} from "./collection/collection-search-results.component"
import {CollectionPublicComponent} from "./collection/detail/collection-public/collection-public.component"
import {ManageCollectionComponent} from "./collection/detail/manage-collection.component"
import {PublishCollectionComponent} from "./collection/detail/publish-collection.component"
import {CollectionSkillSearchComponent} from "./collection/collection-skill-search.component"
import {BatchImportComponent} from "./richskill/import/batch-import.component"
import { ActionByRoles, ButtonAction } from "./auth/auth-roles"
import {MyWorkspaceComponent} from "./my-workspace/my-workspace.component"
import {ConvertToCollectionComponent} from "./my-workspace/convert-to-collection/convert-to-collection.component"
import { MetadataListComponent } from "./metadata/detail/metadata-list/metadata-list.component"
import { DetailCardComponent } from "./detail-card/detail-card.component"
import { MetadataFormComponent } from "./metadata/form/metadata-form.component"
import { MetadataManageComponent } from "./metadata/detail/metadata-manage/metadata-manage.component"
import { MetadataPublicComponent } from "./metadata/detail/metadata-public/metadata-public.component"
import { CreateNamedReferenceComponent } from "./metadata/named-reference/create/create-named-reference.component"


const routes: Routes = [
  { path: "", redirectTo: "/skills", pathMatch: "full" },

  /* SKILLS */

  // create skill
  {path: "skills/create",
    component: RichSkillFormComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.SkillCreate)
    },
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
    data: {
      roles: ActionByRoles.get(ButtonAction.SkillUpdate)
    },
    canDeactivate: [FormDirtyGuard]
  },
  // clone skill
  {path: "skills/:uuid/duplicate",
    component: RichSkillFormComponent,
    canActivate: [AuthGuard],
    canDeactivate: [FormDirtyGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.SkillCreate)
    },
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
  // batch import
  {path: "skills/import",
    component: BatchImportComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.SkillCreate)
    },
  },

  /* KEYWORDS */

  {path: "metadata",
    component: MetadataListComponent,
    canActivate: [AuthGuard],
  },
  // create metadata
  {path: "named-references/create",
    component: CreateNamedReferenceComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.MetadataCreate)
    },
  },
  /*{path: "job-codes/create",
    component: MetadataFormComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.MetadataCreate)
    },
  },*/
  // edit metadata
  /*{path: "named-references/:id/edit",
    component: MetadataFormComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.MetadataUpdate)
    },
  },*/
  /*{path: "job-codes/:id/edit",
    component: MetadataFormComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.MetadataUpdate)
    },
  },*/
  // public metadata detail
  {path: "named-references/:id",
    component: MetadataPublicComponent,
    canActivate: [AuthGuard]
  },
  {path: "job-codes/:id",
    component: MetadataPublicComponent,
    canActivate: [AuthGuard]
  },
  // admin metadata detail
  {path: "named-references/:id/manage",
    component: MetadataManageComponent,
    canActivate: [AuthGuard]
  },
  {path: "job-codes/:id/manage",
    component: MetadataManageComponent,
    canActivate: [AuthGuard]
  },

  /* COLLECTIONS */

  // create collection
  {path: "collections/create",
    component: CollectionFormComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.CollectionCreate)
    },
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
    data: {
      roles: ActionByRoles.get(ButtonAction.CollectionUpdate)
    },
    canDeactivate: [FormDirtyGuard]
  },
  // manage collection
  {path: "collections/:uuid/manage",
    component: ManageCollectionComponent,
    canActivate: [AuthGuard]
  },
  // publish collection guard wizard
  {path: "collections/:uuid/publish",
    component: PublishCollectionComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.CollectionPublish)
    },
  },
  // find skills to add to a collection
  {path: "collections/:uuid/add-skills",
    component: CollectionSkillSearchComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.CollectionSkillsUpdate)
    },
  },
  // find a collection to add a selection of skills to
  {path: "collections/add-skills",
    component: AddSkillsCollectionComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.CollectionSkillsUpdate)
    },
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
  {
    path: "my-workspace",
    component: MyWorkspaceComponent,
    canActivate: [AuthGuard]
  },
  {
    path: "my-workspace/:uuid/add-skills",
    component: CollectionSkillSearchComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.MyWorkspace)
    }
  },
  {
    path: "my-workspace/convert-to-collection",
    component: ConvertToCollectionComponent,
    canActivate: [AuthGuard],
    data: {
      roles: ActionByRoles.get(ButtonAction.MyWorkspace)
    }
  },
  {
    path: "metadata",
    component: MetadataListComponent,
    canActivate: [AuthGuard],
  },
  /* PUBLIC VIEWS */
  {path: "skills/:uuid", component: RichSkillPublicComponent},
  {path: "collections/:uuid", component: CollectionPublicComponent},
  {path: "api/skills/:uuid", component: RichSkillPublicComponent},
  {path: "api/collections/:uuid", component: CollectionPublicComponent},

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
