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
import {CommonModule} from "@angular/common"
import {StatusBarComponent} from "./core/status-bar.component"
import {CardDetailTitleComponent} from "./detail-card/title/card-detail-title.component"
import {TableComponent} from "./table/table.component"
import {SkillListRowComponent} from "./richskill/list/skill-list-row.component"
import {TableLabelComponent} from "./table/table-label.component"
import {LabelWithFilterComponent} from "./table/label-with-filter.component"
import {AccordianComponent} from "./core/accordian.component"
import {FilterControlsComponent} from "./table/filter-controls/filter-controls.component"
import {FilterChoiceComponent} from "./table/filter-controls/filter-choice.component"
import {RichSkillSearchResultsComponent} from "./search/rich-skill-search-results.component";
import {PaginationComponent} from "./table/pagination.component";
import {ActionBarItemComponent} from "./table/action-bar-item.component";
import {TableActionBarComponent} from "./table/table-action-bar.component";
import {EmptyMessageComponent} from "./table/empty-message.component";
import {SkillsListComponent} from "./richskill/list/skills-list.component";
import {AdvancedSearchComponent} from "./search/advanced-search/advanced-search.component"
import {AdvancedSearchHorizontalActionBarComponent} from "./search/advanced-search/action-bar/advanced-search-horizontal-action-bar.component"
import {AdvancedSearchVerticalActionBarComponent} from "./search/advanced-search/action-bar/advanced-search-vertical-action-bar.component"
import {AbstractAdvancedSearchActionBarComponent} from "./search/advanced-search/action-bar/abstract-advanced-search-action-bar.component"
import {StatusPillComponent} from "./core/status-pill.component";
import {DotsMenuComponent} from "./table/dots-menu.component";


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
    StatusBarComponent,
    CardDetailTitleComponent,
    AccordianComponent,
    TableComponent,
    SkillListRowComponent,
    TableLabelComponent,
    TableActionBarComponent,
    ActionBarItemComponent,
    LabelWithFilterComponent,
    FilterControlsComponent,
    FilterChoiceComponent,
    PaginationComponent,
    EmptyMessageComponent,
    SkillsListComponent,
    AdvancedSearchComponent,
    AdvancedSearchVerticalActionBarComponent,
    AdvancedSearchHorizontalActionBarComponent,
    AbstractAdvancedSearchActionBarComponent,
    StatusPillComponent,
    DotsMenuComponent
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
