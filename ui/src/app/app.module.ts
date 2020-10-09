import {BrowserModule, Title} from "@angular/platform-browser"
import {APP_INITIALIZER, NgModule} from "@angular/core"
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {HttpClientModule} from "@angular/common/http"
import {RichSkillsComponent} from "./richskill/detail/rich-skills.component"
import {RichSkillComponent} from "./richskill/detail/rich-skill.component"
import {RichSkillsCsvExportComponent} from "./richskill/task/rich-skills-csv-export.component"
import {AppConfig} from "./app.config"
import {RichSkillFormComponent, SkillFormDirtyGuard} from "./richskill/form/rich-skill-form.component"
import {ReactiveFormsModule} from "@angular/forms"
import {FormField} from "./form/form-field.component"
import {FormFieldText} from "./form/form-field-text.component"
import {FormFieldTextArea} from "./form/form-field-textarea.component"
import {LoadingObservablesDirective} from "./loading/loading-observables.directive"
import {LoadingComponent} from "./loading/loading.component"
import {FormFieldSubmit} from "./form/form-field-submit.component"
import { CommoncontrolsComponent } from "./commoncontrols/commoncontrols.component"
import { AppHeaderComponent } from "./app-header/app-header.component"
import { AppFooterComponent } from "./app-footer/app-footer.component"
import {CommoncontrolsMobileComponent} from "./commoncontrols/commoncontrols-mobile.component"
import {SkillCollectionsDisplayComponent} from "./richskill/form/skill-collections-display.component"
import {ToastComponent} from "./toast/toast.component"
import {ToastService} from "./toast/toast.service"
import { ActionBarComponent } from "./richskill/action-bar/action-bar.component"
import { DetailCardComponent } from "./detail-card/detail-card.component"
import { DetailCardSectionComponent } from "./detail-card/section/section.component"
import { ActionBarHorizontalComponent } from "./richskill/action-bar/action-bar-horizontal/action-bar-horizontal.component"

export function initializeApp(appConfig: AppConfig): () => void {
  return () => appConfig.load()
}

@NgModule({
  declarations: [
    AppComponent,
    RichSkillComponent,
    RichSkillsComponent,
    RichSkillsCsvExportComponent,
    RichSkillFormComponent,
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea,
    LoadingComponent,
    LoadingObservablesDirective,
    CommoncontrolsComponent,
    CommoncontrolsMobileComponent,
    AppHeaderComponent,
    AppFooterComponent,
    SkillCollectionsDisplayComponent,
    ToastComponent,
    ActionBarComponent,
    DetailCardComponent,
    DetailCardSectionComponent,
    ActionBarHorizontalComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
  ],
  providers: [
    Title,
    AppConfig,
    SkillFormDirtyGuard,
    { provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppConfig], multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
