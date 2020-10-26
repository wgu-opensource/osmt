import {BrowserModule, Title} from "@angular/platform-browser"
import {APP_INITIALIZER, NgModule} from "@angular/core"
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {HttpClientModule} from "@angular/common/http"
import {RichSkillsLibraryComponent} from "./richskill/library/rich-skills-library.component"
import {RichSkillPublicComponent} from "./richskill/detail/rich-skill-public/rich-skill-public.component"
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
import {HeaderComponent} from "./navigation/header.component"
import {FooterComponent} from "./navigation/footer.component"
import {SkillCollectionsDisplayComponent} from "./richskill/form/skill-collections-display.component"
import {ToastComponent} from "./toast/toast.component"
import {AuthService} from "./auth/auth-service"
import {AuthGuard} from "./auth/auth.guard"
import {CommoncontrolsComponent} from "./navigation/commoncontrols.component"
import {CommoncontrolsMobileComponent} from "./navigation/commoncontrols-mobile.component"
import {DetailCardComponent} from "./detail-card/detail-card.component"
import {DetailCardSectionComponent} from "./detail-card/section/section.component"
import {PublicSkillActionBarVerticalComponent} from "./richskill/detail/rich-skill-public/action-bar/action-bar-vertical/public-skill-action-bar-vertical.component"
import {PublicSkillActionBarHorizontalComponent} from "./richskill/detail/rich-skill-public/action-bar/action-bar-horizontal/public-skill-action-bar-horizontal.component"
import {RichSkillManageComponent} from "./richskill/detail/rich-skill-manage/rich-skill-manage.component"
import {ManageSkillActionBarVerticalComponent} from "./richskill/detail/rich-skill-manage/action-bar/action-bar-vertical/manage-skill-action-bar-vertical.component"
import {ManageSkillActionBarHorizontalComponent} from "./richskill/detail/rich-skill-manage/action-bar/action-bar-horizontal/manage-skill-action-bar-horizontal.component"
import {ServerErrorComponent} from "./loading/server-error.component"
import { CommonModule } from "@angular/common"
import {DetailCardStatusBarComponent} from "./detail-card/status-bar/detail-card-status-bar.component"
import {CardDetailTitleComponent} from "./detail-card/title/card-detail-title.component"
import { TableComponent } from "./table/table.component"
import { TableRowComponent } from "./table/table-row/table-row.component"
import { TableHeaderComponent } from "./table/table-header/table-header.component"
import { TableLabelComponent } from "./table/table-label/table-label.component"
import { LabelWithFilterComponent } from "./table/table-header/label-with-filter/label-with-filter.component"
import {AccordianComponent} from "./table/table-row/accordian/accordian.component"
import { FilterControlsComponent } from "./table/filter-controls/filter-controls.component"
import { FilterChoiceComponent } from "./table/filter-controls/filter-choice/filter-choice.component"
import {RichSkillSearchResultsComponent} from "./search/rich-skill-search-results.component";
import {PaginationComponent} from "./table/pagination.component";


export function initializeApp(appConfig: AppConfig): () => void {
  return () => appConfig.load()
}

@NgModule({
  declarations: [
    AppComponent,
    LoadingComponent,
    LoadingObservablesDirective,
    ServerErrorComponent,
    CommoncontrolsComponent,
    CommoncontrolsMobileComponent,
    HeaderComponent,
    FooterComponent,
    SkillCollectionsDisplayComponent,
    ToastComponent,

    // Rich skill form
    RichSkillFormComponent,
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea,

    // Rich skills
    RichSkillsLibraryComponent,
    RichSkillsCsvExportComponent,
    SkillCollectionsDisplayComponent,

    // Rich skill detail
    RichSkillPublicComponent,
    PublicSkillActionBarVerticalComponent,
    PublicSkillActionBarHorizontalComponent,

    RichSkillManageComponent,
    ManageSkillActionBarVerticalComponent,
    ManageSkillActionBarHorizontalComponent,

    RichSkillSearchResultsComponent,

    DetailCardComponent,
    DetailCardSectionComponent,
    DetailCardStatusBarComponent,
    CardDetailTitleComponent,
    AccordianComponent,
    TableComponent,
    TableRowComponent,
    TableHeaderComponent,
    TableLabelComponent,
    LabelWithFilterComponent,
    FilterControlsComponent,
    FilterChoiceComponent,
    PaginationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    CommonModule
  ],
  providers: [
    Title,
    AppConfig,
    SkillFormDirtyGuard,
    AuthService,
    AuthGuard,
    { provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppConfig], multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
