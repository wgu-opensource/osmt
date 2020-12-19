import {BrowserModule, Title} from "@angular/platform-browser"
import {APP_INITIALIZER, NgModule} from "@angular/core"
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {HttpClientModule} from "@angular/common/http"
import {RichSkillsLibraryComponent} from "./richskill/library/rich-skills-library.component"
import {RichSkillPublicComponent} from "./richskill/detail/rich-skill-public/rich-skill-public.component"
import {RichSkillsCsvExportComponent} from "./richskill/task/rich-skills-csv-export.component"
import {AppConfig} from "./app.config"
import {RichSkillFormComponent} from "./richskill/form/rich-skill-form.component"
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
import {SkillTableComponent} from "./table/skills-library-table/skill-table.component"
import {SkillListRowComponent} from "./richskill/list/skill-list-row.component"
import {TableLabelComponent} from "./table/skills-library-table/table-label.component"
import {LabelWithFilterComponent} from "./table/skills-library-table/label-with-filter.component"
import {AccordianComponent} from "./core/accordian.component"
import {FilterControlsComponent} from "./table/filter-controls/filter-controls.component"
import {FilterChoiceComponent} from "./table/filter-controls/filter-choice.component"
import {RichSkillSearchResultsComponent} from "./search/rich-skill-search-results.component"
import {PaginationComponent} from "./table/skills-library-table/pagination.component"
import {ActionBarItemComponent} from "./table/skills-library-table/action-bar-item.component"
import {TableActionBarComponent} from "./table/skills-library-table/table-action-bar.component"
import {EmptyMessageComponent} from "./table/skills-library-table/empty-message.component"
import {SkillsListComponent} from "./richskill/list/skills-list.component"
import {AdvancedSearchComponent} from "./search/advanced-search/advanced-search.component"
import {AdvancedSearchHorizontalActionBarComponent} from "./search/advanced-search/action-bar/advanced-search-horizontal-action-bar.component"
import {AdvancedSearchVerticalActionBarComponent} from "./search/advanced-search/action-bar/advanced-search-vertical-action-bar.component"
import {AbstractAdvancedSearchActionBarComponent} from "./search/advanced-search/action-bar/abstract-advanced-search-action-bar.component"
import {DotsMenuComponent} from "./table/skills-library-table/dots-menu.component"
import {AddSkillsCollectionComponent} from "./collection/add-skills-collection.component"
import {CollectionTableComponent} from "./collection/collection-table.component"
import {CollectionListRowComponent} from "./collection/collection-list-row.component"
import {CollectionFormComponent} from "./collection/create-collection/collection-form.component"
import {AbstractCreateCollectionActionbarComponent} from "./collection/create-collection/action-bar/abstract-create-collection-actionbar.component"
import {CreateCollectionActionBarHorizontalComponent} from "./collection/create-collection/action-bar/create-collection-action-bar-horizontal.component"
import {CreateCollectionActionBarVerticalComponent} from "./collection/create-collection/action-bar/create-collection-action-bar-vertical.component"
import {AbstractTableComponent} from "./table/abstract-table.component"
import {CollectionPublicComponent} from "./collection/detail/collection-public/collection-public.component"
import {PublicCollectionDetailCardComponent} from "./collection/detail/collection-public/public-collection-detail-card.component"
import {CollectionPublicHorizontalActionBarComponent} from "./collection/detail/collection-public/action-bar/horizontal/collection-public-horizontal-action-bar.component"
import {CollectionPublicVerticalActionBarComponent} from "./collection/detail/collection-public/action-bar/vertical/collection-public-vertical-action-bar.component"
import {CollectionPublicActionBarComponent} from "./collection/detail/collection-public/action-bar/collection-public-action-bar.component"
import {PublicTableComponent} from "./table/public-table/public-table.component"
import {FormDirtyGuard} from "./core/abstract-form.component"
import {CollectionsLibraryComponent} from "./table/collections-library.component"
import {CollectionsListComponent} from "./collection/collections-list.component"
import {CollectionSearchResultsComponent} from "./collection/collection-search-results.component"
import {PublicRichSkillActionBarComponent} from "./richskill/detail/rich-skill-public/action-bar/public-rich-skill-action-bar.component"
import {ManageCollectionComponent} from "./collection/detail/manage-collection.component"
import {VerticalActionBarComponent} from "./core/vertical-action-bar.component"
import {PublishCollectionComponent} from "./collection/detail/publish-collection.component"
import {BlockingLoaderComponent} from "./core/blocking-loader.component"
import {CollectionSkillSearchComponent} from "./collection/collection-skill-search.component"
import {BatchImportComponent} from "./richskill/import/batch-import.component"
import {FieldMappingSelectComponent, FieldMappingTableComponent} from "./richskill/import/field-mapping-table.component"
import {
  ImportPreviewTableComponent,
  InlineErrorComponent,
  InlineHeadingComponent,
  NamedReferenceComponent
} from "./richskill/import/import-preview-table.component"
import {FormFieldSearchSelectComponent} from "./form/form-field-search-select/single-select/form-field-search-select.component"
import {FormFieldSearchMultiSelectComponent} from "./form/form-field-search-select/mulit-select/form-field-search-multi-select.component"
import {FormFieldSearchSelectJobcodeComponent} from "./form/form-field-search-select/jobcode-select/form-field-search-select-jobcode.component"
import {AuditLogComponent} from "./richskill/detail/audit-log.component"
import { OccupationsCardSectionComponent } from "./richskill/detail/occupations-card-section/occupations-card-section.component"

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

    AddSkillsCollectionComponent,
    CollectionTableComponent,
    CollectionListRowComponent,
    CollectionsLibraryComponent,
    CollectionsListComponent,
    CollectionSearchResultsComponent,
    PublishCollectionComponent,

    DetailCardComponent,
    DetailCardSectionComponent,
    StatusBarComponent,
    CardDetailTitleComponent,
    AccordianComponent,
    AbstractTableComponent,
    SkillTableComponent,
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
    DotsMenuComponent,
    AbstractAdvancedSearchActionBarComponent,
    CollectionFormComponent,
    AbstractCreateCollectionActionbarComponent,
    CreateCollectionActionBarHorizontalComponent,
    CreateCollectionActionBarVerticalComponent,
    PublicTableComponent,
    CollectionPublicComponent,
    PublicCollectionDetailCardComponent,
    CollectionPublicHorizontalActionBarComponent,
    CollectionPublicVerticalActionBarComponent,
    CollectionPublicActionBarComponent,
    PublicRichSkillActionBarComponent,
    ManageCollectionComponent,
    VerticalActionBarComponent,
    BlockingLoaderComponent,
    CollectionSkillSearchComponent,
    BatchImportComponent,
    FieldMappingTableComponent,
    FieldMappingSelectComponent,
    ImportPreviewTableComponent,
    InlineHeadingComponent,
    NamedReferenceComponent,
    InlineErrorComponent,
    FormFieldSearchSelectComponent,
    FormFieldSearchMultiSelectComponent,
    FormFieldSearchSelectJobcodeComponent,
    AuditLogComponent,
    OccupationsCardSectionComponent
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
    FormDirtyGuard,
    AuthService,
    AuthGuard,
    { provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppConfig], multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
