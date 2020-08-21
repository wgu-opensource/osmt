import {NgModule} from "@angular/core"
import {Routes, RouterModule} from "@angular/router"
import {RichskillComponent} from "./richskill/detail/richskill.component"

const routes: Routes = [
  {
    path: "skills/:uuid",
    component: RichskillComponent
  }
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
