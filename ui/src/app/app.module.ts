import {BrowserModule, Title} from "@angular/platform-browser"
import {NgModule} from "@angular/core"
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {HttpClientModule} from "@angular/common/http"
import {RichSkillsComponent} from "./richskill/detail/rich-skills.component"
import {RichSkillComponent} from "./richskill/detail/rich-skill.component"
import {RichSkillsCsvExportComponent} from "./richskill/task/rich-skills-csv-export.component"

@NgModule({
  declarations: [
    AppComponent,
    RichSkillComponent,
    RichSkillsComponent,
    RichSkillsCsvExportComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
  ],
  providers: [Title],
  bootstrap: [AppComponent]
})
export class AppModule {
}
