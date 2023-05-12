import {ComponentFixture, TestBed} from "@angular/core/testing"

import {MyWorkspaceComponent} from "./my-workspace.component"
import {RouterTestingModule} from "@angular/router/testing"
import {AuthService} from "../auth/auth-service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {AuthServiceStub, CollectionServiceStub, EnvironmentServiceStub, RichSkillServiceStub} from "../../../test/resource/mock-stubs"
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {AppConfig} from "../app.config"
import {By, Title} from "@angular/platform-browser"
import {ToastService} from "../toast/toast.service"
import {EnvironmentService} from "../core/environment.service"
import {CollectionService} from "../collection/service/collection.service"
import {Router} from "@angular/router"
import {ManageCollectionComponent} from "../collection/detail/manage-collection.component"
import {createMockCollection, createMockSkillSummary} from "../../../test/resource/mock-data"
import {PublishStatus} from "../PublishStatus"
import {PublicCollectionDetailCardComponent} from "../collection/detail/collection-public/public-collection-detail-card.component"
import {VerticalActionBarComponent} from "../core/vertical-action-bar.component"
import {FilterControlsComponent} from "../table/filter-controls/filter-controls.component"
import {FilterChoiceComponent} from "../table/filter-controls/filter-choice.component"
import {TableActionBarComponent} from "../table/skills-library-table/table-action-bar.component"
import {ActionBarItemComponent} from "../table/skills-library-table/action-bar-item.component"
import {CommonModule} from "@angular/common"
import {ReactiveFormsModule} from "@angular/forms"
import {LoadingObservablesDirective} from "../loading/loading-observables.directive"
import {SkillTableComponent} from "../table/skills-library-table/skill-table.component"
import {PaginationComponent} from "../table/skills-library-table/pagination.component"
import {LabelWithSelectComponent} from "../table/skills-library-table/label-with-select.component"
import {AuditLogComponent} from "../richskill/detail/audit-log.component"
import {LabelWithFilterComponent} from "../table/skills-library-table/label-with-filter.component"
import {SkillListRowComponent} from "../richskill/list/skill-list-row.component"
import {StatusBarComponent} from "../core/status-bar.component"
import {DotsMenuComponent} from "../table/skills-library-table/dots-menu.component"
import { ConvertToCollectionComponent } from "./convert-to-collection/convert-to-collection.component"

describe("MyWorkspaceComponent", () => {
  let component: MyWorkspaceComponent
  let fixture: ComponentFixture<MyWorkspaceComponent>
  let collectionService: CollectionService

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([
          {
            path: "my-workspace/uuid1/add-skills",
            component: ManageCollectionComponent
          },
          {
            path: "my-workspace/convert-to-collection",
            component: ConvertToCollectionComponent
          }
        ]),
        HttpClientTestingModule,
        CommonModule,
        ReactiveFormsModule
      ],
      declarations: [
        MyWorkspaceComponent,
        PublicCollectionDetailCardComponent,
        VerticalActionBarComponent,
        FilterControlsComponent,
        FilterChoiceComponent,
        TableActionBarComponent,
        ActionBarItemComponent,
        LoadingObservablesDirective,
        SkillTableComponent,
        PaginationComponent,
        LabelWithSelectComponent,
        LabelWithFilterComponent,
        AuditLogComponent,
        SkillListRowComponent,
        StatusBarComponent,
        DotsMenuComponent,
      ],
      providers: [
        AppConfig,
        Title,
        ToastService,
        {provide: EnvironmentService, useClass: EnvironmentServiceStub},  // Example of using a service stub
        {provide: RichSkillService, useClass: RichSkillServiceStub},
        {provide: CollectionService, useClass: CollectionServiceStub},
        {provide: AuthService, useClass: AuthServiceStub},
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(MyWorkspaceComponent)
    component = fixture.componentInstance
    collectionService  = TestBed.inject(CollectionService)
    fixture.detectChanges()
  })

  it("reload collection should call get workspace", () => {
    const spy = spyOn(collectionService, "getWorkspace").and.callThrough()
    component.reloadCollection()
    expect(spy).toHaveBeenCalled()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("actions definitions should be correct", () => {
    expect(component.actionDefinitions().length).toEqual(4)
  })

  it("handle confirm delete collection", () => {
    const spy = spyOn(component, "submitSkillRemoval").and.callThrough()
    component.handleConfirmDeleteCollection()
    expect(spy).toHaveBeenCalled()
    expect(component.template).toEqual("default")
  })

  it("convert to collection action", () => {
    const router = TestBed.inject(Router)
    const spyNavigate = spyOn(router, "navigate").and.callThrough()
    const spyLocalStorage = spyOn(localStorage, "setItem").and.callThrough()
    component["convertToCollectionAction"]()
    expect(spyLocalStorage).toHaveBeenCalled()
    expect(spyNavigate).toHaveBeenCalled()
  })

  it("confirm message text", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component.confirmMessageText).toBe("reset My Workspace")
  })

  it("confirm button text", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component.confirmButtonText).toBe("reset My Workspace")
  })

  it("workspace is not empty", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component["workspaceEmpty"]()).toBeFalse()
  })

  it("router should navigate correctly", () => {
    const router = TestBed.inject(Router)
    const spy = spyOn(router, "navigate").and.resolveTo(true)
    component.addSkillsAction()
    expect(spy).toHaveBeenCalledWith(["/my-workspace/uuid1/add-skills"])
  })

  it("add to collection should not be visible", () => {
    expect(component.addToCollectionVisible()).toBeFalse()
  })

  it("add to collection should be visible", () => {
    component.selectedSkills = [createMockSkillSummary()]
    expect(component.addToCollectionVisible()).toBeTrue()
  })

  it("show log should be false", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    const auditLog = fixture.debugElement.query(By.css("app-audit-log"))
    expect(component.showLog).toBeFalse()
    expect(auditLog).toBeFalsy()
  })

  it("Download should have submenu", () => {
    const actionDownload = component.actionDefinitions()[1]
    expect(actionDownload?.menu?.length).toEqual(2)
  })

  it("Menu action download should call callback correctly", () => {
    const actionDownload = component.actionDefinitions()[1]
    const downloadXlsx = actionDownload.menu?.pop()
    const downloadCsv = actionDownload.menu?.pop()
    const spyDownloadCsv = spyOn(component.exporter, "getCollectionCsv")
    const spyDownloadXlsx = spyOn(component.exporter, "getCollectionXlsx")
    expect(actionDownload).toBeTruthy()
    if (downloadCsv?.callback && actionDownload) {
      downloadCsv.callback(actionDownload)
      expect(spyDownloadCsv).toHaveBeenCalled()
    }
    if (downloadXlsx?.callback && actionDownload) {
      downloadXlsx.callback(actionDownload)
      expect(spyDownloadXlsx).toHaveBeenCalled()
    }
  })

})
