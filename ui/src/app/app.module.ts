import {BrowserModule, Title} from "@angular/platform-browser"
import {APP_INITIALIZER, NgModule} from "@angular/core"
import {AppRoutingModule} from "./app-routing.module"
import {AppComponent} from "./app.component"
import {HttpClientModule} from "@angular/common/http"
import {LoginSuccessComponent} from "./auth/login-success.component"
import {EnvironmentService} from "./core/environment.service"
import {RichSkillsLibraryComponent} from "./richskill/library/rich-skills-library.component"
import {RichSkillPublicComponent} from "./richskill/detail/rich-skill-public/rich-skill-public.component"
import {AppConfig} from "./app.config"
import {RichSkillFormComponent} from "./richskill/form/rich-skill-form.component"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {LoadingObservablesDirective} from "./loading/loading-observables.directive"
import {LoadingComponent} from "./loading/loading.component"
import {HeaderComponent} from "./navigation/header.component"
import {FooterComponent} from "./navigation/footer.component"
import {SkillCollectionsDisplayComponent} from "./richskill/form/skill-collections-display.component"
import {ToastComponent} from "./toast/toast.component"
import {PillComponent} from "./core/pill/pill.component";
import {PillGroupComponent} from "./core/pill/group/pill-group.component";
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
import {SelectLabelComponent} from "./table/label/select/select-label.component"
import {SortLabelComponent} from "./table/label/sort/sort-label.component"
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
import {CategoryDetailComponent} from "./category/detail/category-detail.component"
import {CategoryDetailCardComponent} from "./category/detail/card/category-detail-card.component"
import {CategoryLibraryComponent} from "./category/library/category-library.component"
import {CategoryListComponent} from "./category/list/category-list.component"
import {CategoryTableComponent} from "./category/table/category-table.component"
import {CategoryTableRowComponent} from "./category/table/row/category-table-row.component"
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
  NamedReferenceComponent
} from "./richskill/import/import-preview-table.component"
import {AuditLogComponent} from "./richskill/detail/audit-log.component"
import {OccupationsCardSectionComponent} from "./richskill/detail/occupations-card-section/occupations-card-section.component"
import {CheckerComponent} from "./richskill/form/checker.component"
import {SystemMessageComponent} from "./core/system-message.component"
import {LoginComponent} from "./auth/login.component"
import {LogoutComponent} from "./auth/logout.component"
import {NgIdleKeepaliveModule} from "@ng-idle/keepalive"
import {LabelWithSelectComponent} from "./table/skills-library-table/label-with-select.component"
import {LibraryExportComponent} from "./navigation/libraryexport.component"
import {MyWorkspaceComponent} from "./my-workspace/my-workspace.component"
import {CollectionPipe} from "./pipes"
import { SharedModule } from "@shared/shared.module"
import { OsmtCoreModule } from "./core/osmt-core.module"
import { ExportCollectionComponent } from "./export/export-collection.component"
import { ExportRsdComponent } from "./export/export-rsd.component"
import { OsmtFormModule } from "./form/osmt-form.module"
import { ConvertToCollectionComponent } from "./my-workspace/convert-to-collection/convert-to-collection.component"
import { SizePaginationComponent } from "./table/skills-library-table/size-pagination/size-pagination.component"
import {OsmtTableModule} from "./table/osmt-table.module"
import { getBaseApi } from "./api-versions";
import { SkillImportComponent } from './navigation/skill-import/skill-import.component';
import { InlineHeadingComponent } from './richskill/import/inline-heading/inline-heading.component'
import { InlineErrorComponent } from "./richskill/import/inline-error/inline-error.component"
import { BatchImportCollectionComponent } from './collection/create-collection/batch-import-collection/batch-import-collection.component';

export function initializeApp(
  appConfig: AppConfig,
  authService: AuthService
): () => void {
  // AppConfig.settings is initialized lazily (on the next line), but authService must be initialized sooner.
  authService.init()
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
    SystemMessageComponent,
    LogoutComponent,
    LoginComponent,
    LoginSuccessComponent,
    PillComponent,
    PillGroupComponent,

    // Rich skill form
    RichSkillFormComponent,

    // Rich skills
    RichSkillsLibraryComponent,
    SkillCollectionsDisplayComponent,
    LibraryExportComponent,

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
    ExportCollectionComponent,
    ExportRsdComponent,

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
    SelectLabelComponent,
    SortLabelComponent,
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
    CategoryDetailComponent,
    CategoryDetailCardComponent,
    CategoryLibraryComponent,
    CategoryListComponent,
    CategoryTableComponent,
    CategoryTableRowComponent,
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
    AuditLogComponent,
    OccupationsCardSectionComponent,
    CheckerComponent,
    LabelWithSelectComponent,
    MyWorkspaceComponent,
    CollectionPipe,
    ConvertToCollectionComponent,
    SizePaginationComponent,
    SkillImportComponent,
    BatchImportCollectionComponent,
  ],
  imports: [
    NgIdleKeepaliveModule.forRoot(),
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    CommonModule,
    SharedModule,
    OsmtCoreModule,
    OsmtFormModule,
    FormsModule,
    OsmtTableModule
  ],
  providers: [
    EnvironmentService,
    Title,
    AppConfig,
    FormDirtyGuard,
    AuthService,
    AuthGuard,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AppConfig, AuthService],
      multi: true
    },
    {
      provide: "BASE_API",
      useFactory: () =>  getBaseApi(),
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
