import {BrowserModule, Title} from "@angular/platform-browser"
import {NgModule} from "@angular/core"
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {HttpClientModule} from "@angular/common/http"
import {RichskillComponent} from "./richskill/detail/richskill.component"

@NgModule({
  declarations: [
    AppComponent,
    RichskillComponent
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
