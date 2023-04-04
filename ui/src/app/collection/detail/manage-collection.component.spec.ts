// noinspection MagicNumberJS,LocalVariableNamingConventionJS

import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Component, ElementRef, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { of } from "rxjs"
import {
  createMockCollection,
  createMockPaginatedSkills,
  createMockSkillSummary,
  csvContent
} from "../../../../test/resource/mock-data"
import {
  AuthServiceStub,
  CollectionServiceStub,
  EnvironmentServiceStub,
  RichSkillServiceStub
} from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { initializeApp } from "../../app.module"
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { ApiSkillSummary } from "../../richskill/ApiSkillSummary"
import { ApiSearch, PaginatedSkills } from "../../richskill/service/rich-skill-search.service"
import { RichSkillService } from "../../richskill/service/rich-skill.service"
import { ToastService } from "../../toast/toast.service"
import { ApiCollection } from "../ApiCollection"
import { CollectionService } from "../service/collection.service"
import { ManageCollectionComponent } from "./manage-collection.component"
import {AuthService} from "../../auth/auth-service";
import * as FileSaver from "file-saver"
import * as Auth from "../../auth/auth-roles"
import {CollectionsLibraryComponent} from "../../table/collections-library.component"
import {FormControl, FormGroup} from "@angular/forms"


@Component({
  selector: "app-concrete-component",
  template: ``
})
class ConcreteComponent extends ManageCollectionComponent {
  setSize(size: number): void { super.size = size }
  setFrom(from: number): void { super.from = from }
}


export function createComponent(T: Type<ConcreteComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: ConcreteComponent
let fixture: ComponentFixture<ConcreteComponent>


describe("ManageCollectionComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConcreteComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([
          { path: "collections/uuid1/add-skills", component: ManageCollectionComponent },
          { path: "collections/UUID1/edit", component: ManageCollectionComponent },
          { path: "collections/uuid1/publish", component: ManageCollectionComponent },
          { path: "collections", component: CollectionsLibraryComponent}
        ]),
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        Title,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },  // Example of using a service stub
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    createComponent(ConcreteComponent)
    component.titleElement = new ElementRef(document.getElementById("titleHeading"))
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("collectionHasSkills should be correct", () => {
    component.collection = undefined
    expect(component.collectionHasSkills).toBeFalsy()

    component.collection = new ApiCollection(createMockCollection(
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      PublishStatus.Draft
      // The default is to have some skills
    ))
    expect(component.collectionHasSkills).toBeTruthy()
  })

  it("reloadCollection should be correct", () => {
    // Arrange
    component.uuidParam = "uuid1"
    const collection = new ApiCollection(createMockCollection(
          new Date("2020-06-25T14:58:46.313Z"),
          new Date("2020-06-25T14:58:46.313Z"),
          new Date("2020-06-25T14:58:46.313Z"),
          new Date("2020-06-25T14:58:46.313Z"),
          PublishStatus.Draft
        ))

    // Act
    component.reloadCollection()

    // Assert
    expect(component.collection).toEqual(collection)
  })

  it("loadNextPage should be correct", () => {
    expect(component.collection).toBeTruthy()  // collection should be set via ngOnInit() > reloadCollection()

    // Arrange
    const totalCount = createMockPaginatedSkills().totalCount
    component.results = undefined

    // Act
    component.loadNextPage()
    while (!component.results) {}

    // Assert
    expect((component.results as PaginatedSkills).totalCount).toEqual(totalCount)
  })

  it("loadNextPage should handle undefined", () => {
    // Arrange
    component.collection = undefined
    const collectionService = TestBed.inject(CollectionService)
    spyOn(collectionService, "getCollectionSkills")

    // Act
    component.loadNextPage()

    // Assert
    expect(collectionService.getCollectionSkills).not.toHaveBeenCalled()
  })

  it("getSelectAllCount should be correct", () => {
    expect(component.collection).toBeTruthy()  // collection should be set via ngOnInit() > reloadCollection()

    // Arrange
    const totalCount = createMockPaginatedSkills().totalCount
    component.results = undefined

    // Act
    component.loadNextPage()
    while (!component.results) {}

    // Assert
    expect(component.getSelectAllCount()).toEqual(totalCount)
  })

  it("handleSelectAll should be correct", () => {
    // Arrange
    component.selectAllChecked = false

    // Act
    const result = component.handleSelectAll(true)

    // Assert
    expect(result).toBeFalsy()  // Always false
    expect(component.selectAllChecked).toBeTruthy()
  })

  it("selectedCount (1/2) should count results", () => {
    // Arrange
    component.selectAllChecked = true
    const skills = createMockPaginatedSkills(5, 10)
    component.selectedSkills = [
      createMockSkillSummary("id2")  // just one item
    ]
    component.results = skills

    // Act
    const result = component.selectedCount

    // Assert
    expect(result).toEqual(skills.totalCount)
  })

  it("selectedCount (2/2) should count selected", () => {
    // Arrange
    component.selectAllChecked = false
    const skills = createMockPaginatedSkills(5, 10)
    component.selectedSkills = [
      createMockSkillSummary("id2")  // just one item
    ]
    component.results = skills

    // Act
    const result = component.selectedCount

    // Assert
    expect(result).toEqual(component.selectedSkills.length)
  })

  it("clearSearch should be correct", () => {
    // Arrange
    component.apiSearch = new ApiSearch({})
    component.from = 10
    component.results = undefined

    // Act
    const result = component.clearSearch()
    while (!component.results) {}

    // Assert
    expect(component.from).toBeFalsy()
    expect(component.results).toBeTruthy()
    expect(result).toBeFalsy()
  })

  it("handleDefaultSubmit should be correct", () => {
    // Arrange
    component.searchForm.setValue({search: "my search string"})
    component.apiSearch = undefined
    component.matchingQuery = undefined
    component.from = 1
    spyOn(component, "loadNextPage").and.stub()

    // Act
    const result = component.handleDefaultSubmit()

    // Assert
    expect(result).toBeFalsy()
    expect(component.apiSearch).toBeTruthy()
    expect(component.matchingQuery).toBeTruthy()
    expect(component.from).toEqual(0)
  })

  it("actionDefinitions should be correct", () => {
    const router = TestBed.inject(Router)
    const spyNavigate = spyOn(router, "navigate").and.callThrough()
    spyOn(component, "submitCollectionStatusChange").and.stub();

    [
      { publishDate: new Date("2020-06-25T14:58:46.313Z"), status: PublishStatus.Published, action2Label: "View Published Collection" },
      { publishDate: undefined, status: PublishStatus.Draft, action2Label: "Publish Collection" }
    ].forEach((params) => {
      // Arrange
      component.uuidParam = "UUID1"
      component.collection = new ApiCollection(createMockCollection(
        new Date("2020-06-25T14:58:46.313Z"),
        new Date("2020-06-25T14:58:46.313Z"),
        new Date("2020-06-25T14:58:46.313Z"),
        params.publishDate,
        params.status
        // The default is to have some skills
      ))

      // Act
      const actions = component.actionDefinitions()

      // Assert
      expect(actions).toBeTruthy()
      expect(actions.length).toEqual(6)

      let action = actions[0]
      expect(action.label).toEqual("Add RSDs to This Collection")
      expect(action.primary).toBeFalsy()
      expect(action && action.callback).toBeTruthy()
      spyNavigate.calls.reset()
      action.callback?.(action)
      expect(router.navigate).toHaveBeenCalledWith(["/collections/uuid1/add-skills"])

      action = actions[1]
      expect(action.label).toEqual("Edit Collection")
      expect(action.primary).toBeFalsy()
      expect(action && action.callback).toBeTruthy()
      spyNavigate.calls.reset()
      action.callback?.(action)
      expect(router.navigate).toHaveBeenCalledWith(["/collections/UUID1/edit"])

      action = actions[2]
      expect(action.label).toEqual(params.action2Label)
      expect(action && action.callback).toBeTruthy()

      action = actions[3]
      expect(action.label).toEqual("Archive Collection ")
      expect(action.primary).toBeFalsy()
      expect(action && action.callback).toBeTruthy()
      action.callback?.(action)
      expect(action.visible?.()).toBeTruthy()  // !== PublishStatus.Archived  && !== PublishStatus.Deleted
    })
  })

  it("delete collection should be visible", () => {
    const spy = spyOnProperty(Auth, "ENABLE_ROLES").and.returnValue(true)
    const actions = component.actionDefinitions()
    const action = actions[5]
    expect(action.label).toEqual("Delete Collection")
    expect(actions.length).toEqual(6)
  })

  it("delete collection should not be visible", () => {
    const spy = spyOnProperty(Auth, "ENABLE_ROLES").and.returnValue(false)
    const actions = component.actionDefinitions()
    const action = actions[5]
    expect(action).toBeUndefined()
    expect(actions.length).toEqual(5)
  })

  it("when delete collection action is called template should be confirm-delete-collection", () => {
    component.deleteCollectionAction()
    expect(component.template === "confirm-delete-collection")
  })

  it("handleConfirmDeleteCollection should call", () => {
    const spyLoader = spyOn(component["toastService"].loaderSubject, "next")
    const spyDeleteCollectionWithResult = spyOn(component["collectionService"], "deleteCollectionWithResult").and.callThrough()
    // window.onbeforeunload = jasmine.createSpy()
    component.handleConfirmDeleteCollection()
    expect(spyLoader).toHaveBeenCalledWith(true)
    expect(spyDeleteCollectionWithResult).toHaveBeenCalled()
  })

  it("publishAction should be correct", () => {
    // Arrange for all
    const router = TestBed.inject(Router)
    const collectionService = TestBed.inject(CollectionService)
    const spyNavigate = spyOn(router, "navigate").and.callThrough()
    spyOn(window, "confirm").and.returnValue(true)
    const spySubmitCollectionStatusChange = spyOn(component, "submitCollectionStatusChange").and.stub()
    let readyUuid
    spyOn(collectionService, "collectionReadyToPublish")
      .and.callFake((uuid) => {
        readyUuid = uuid
        return of(uuid !== "uuid1")
      })

    // Arrange
    const skill1 = createMockSkillSummary("id1")
    component.uuidParam = skill1.uuid
    // Act
    component.publishAction()
    while (!readyUuid) {}
    // Assert
    expect(component.submitCollectionStatusChange).not.toHaveBeenCalled()
    expect(router.navigate).toHaveBeenCalledWith(["/collections/uuid1/publish"])

    // Arrange
    spyNavigate.calls.reset()
    spySubmitCollectionStatusChange.calls.reset()
    readyUuid = undefined
    const skill2 = createMockSkillSummary("id2")
    component.uuidParam = skill2.uuid
    // Act
    component.publishAction()
    while (!readyUuid) {}
    // Assert
    expect(component.submitCollectionStatusChange).toHaveBeenCalled()
    expect(router.navigate).not.toHaveBeenCalled()

    // Arrange
    spyNavigate.calls.reset()
    spySubmitCollectionStatusChange.calls.reset()
    component.uuidParam = undefined
    // Act
    component.publishAction()
    // Assert
    expect(component.submitCollectionStatusChange).not.toHaveBeenCalled()
    expect(router.navigate).not.toHaveBeenCalled()
  })

  it("submitCollectionStatusChange should be correct", () => {
    // Arrange
    const uuid = "uuid1"
    const status = PublishStatus.Draft
    const collection = new ApiCollection(createMockCollection(
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      PublishStatus.Draft
      // The default is to have some skills
    ))
    const collectionService = TestBed.inject(CollectionService)
    component.uuidParam = uuid
    component.results = undefined
    // An example of a one-off service stub.  If reused, then put it in mock-stubs.ts
    spyOn(collectionService, "updateCollection").and.callFake(
      (theUuid, updateObject) => {
        expect(theUuid).toEqual(uuid)
        expect(updateObject.status).toEqual(status)
        return of(collection)
      }
    )

    // Act
    component.submitCollectionStatusChange(PublishStatus.Draft, "whatever")

    // Assert
    while (!component.results) {}
    expect(component.results).toEqual(createMockPaginatedSkills())
    expect(component.collection).toEqual(collection)
  })

  it("submitCollectionStatusChange should handle undefined", () => {
    // Arrange
    const collectionService = TestBed.inject(CollectionService)
    component.uuidParam = undefined
    spyOn(collectionService, "updateCollection")

    // Act
    component.submitCollectionStatusChange(PublishStatus.Draft, "whatever")

    // Assert
    expect(collectionService.updateCollection).not.toHaveBeenCalled()
  })

  it("removeFromCollection should be correct", () => {
    // Arrange for allo
    const spySubmitSkillRemoval = spyOn(component, "submitSkillRemoval").and.returnValue()  // Test just this method
    const spyGetApiSearch = spyOn(component, "getApiSearch").and.callThrough()
    spyOn(window, "confirm").and.returnValue(true)
    const skill = new ApiSkillSummary(createMockSkillSummary("id1"))
    const skills = createMockPaginatedSkills(5, 10)
    component.selectedSkills = [
      createMockSkillSummary("id2"),
      createMockSkillSummary("id3")
    ]
    component.results = skills
    component.template = "default"
    component.selectAllChecked = false

    // Act
    component.removeFromCollection(skill)
    // Assert
    expect(component.template).toEqual("default")
    expect(component.submitSkillRemoval).toHaveBeenCalled()

    // Arrange
    spySubmitSkillRemoval.calls.reset()
    // Act
    component.removeFromCollection()
    // Assert
    expect(component.template).toEqual("confirm-multiple")
    expect(component.submitSkillRemoval).not.toHaveBeenCalled()

    // Arrange
    spySubmitSkillRemoval.calls.reset()
    component.selectAllChecked = true
    // Act
    component.removeFromCollection(skill)
    // Assert
    expect(component.template).toEqual("confirm-multiple")
    expect(component.submitSkillRemoval).not.toHaveBeenCalled()

    // Arrange
    spyGetApiSearch.calls.reset()
    component.uuidParam = undefined
    // Act
    component.removeFromCollection(skill)
    // Assert
    expect(component.getApiSearch).not.toHaveBeenCalled()
  })

  it("remove skills should have api search with query", () => {
    component.selectAllChecked = true
    const searchQuery = "art"
    component.searchForm?.get("search")?.patchValue(searchQuery)
    component.removeFromCollection()
    expect(component.apiSearch?.query).toEqual(searchQuery)
  })

  it("remove skills should have api search with query", () => {
    component.selectAllChecked = true
    component.removeFromCollection()
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component.apiSearch?.uuids?.length).toBeGreaterThan(0)
  })

  it("submitSkillRemoval should be correct", () => {
    // Arrange
    const spyReloadCollection = spyOn(component, "reloadCollection").and.returnValue()
    const spyLoadNextPage = spyOn(component, "loadNextPage").and.returnValue()
    const apiSearch = new ApiSearch({})

    // Act
    component.submitSkillRemoval(apiSearch)

    // Assert
    expect(component.reloadCollection).toHaveBeenCalled()
    expect(component.loadNextPage).toHaveBeenCalled()
  })

  it("handleClickConfirmMulti should be correct", () => {
    // Arrange
    const spySubmitSkillRemoval = spyOn(component, "submitSkillRemoval").and.returnValue()  // Test just this method
    component.template = "confirm-multiple"
    component.apiSearch = new ApiSearch({})

    // Act
    const result = component.handleClickConfirmMulti()

    // Assert
    expect(result).toBeFalsy()
    expect(component.template).toEqual("default")
    expect(component.apiSearch).toBeFalsy()
    expect(component.submitSkillRemoval).toHaveBeenCalled()
  })

  it("handleClickCancel should be correct", () => {
    // Arrange
    component.template = "confirm-multiple"
    component.apiSearch = new ApiSearch({})

    // Act
    component.handleClickCancel()

    // Assert
    expect(component.template).toEqual("default")
    expect(component.apiSearch).toBeFalsy()
  })

  it("generateCsv should call getCsv and loader", () => {
    const spyCollectionService = spyOn(component["collectionService"], "requestCollectionSkillsCsv").and.callThrough()
    const spyLoaderSubject = spyOn(component["toastService"].loaderSubject, "next")
    component.generateCsv("My collection")
    expect(spyCollectionService).toHaveBeenCalled()
    expect(spyLoaderSubject).toHaveBeenCalledWith(true)
  })


  it("getCsv should call getCsvTaskResultsIfComplete", () => {
    const collection = {
      uuid: "fc0a65a6-facd-4f9d-b590-cfecbfe706ad",
      name: "My Collection"
    }
    const spyCollectionService = spyOn(component["collectionService"], "getCsvTaskResultsIfComplete").and.returnValue(of(csvContent))
    const spySaveCsv = spyOn(component, "saveCsv")
    component.getCsv(collection.uuid, collection.name)
    expect(spyCollectionService).toHaveBeenCalledWith(collection.uuid)
    expect(spySaveCsv).toHaveBeenCalledWith(csvContent.body, collection.name)
  })

  it("saveCSV should call FileSaver", () => {
    const spySaveAS = spyOn(FileSaver, "saveAs")
    component.saveCsv(csvContent.body, "My Collection")
    expect(spySaveAS).toHaveBeenCalled()
  })

  it("confirm message text", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    component.collection.name = "Test Collection"
    expect(component.confirmMessageText).toBe("delete Test Collection")
  })

  it("confirm button text", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component.confirmButtonText).toBe("delete collection")
  })

  it("show log should be true", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Draft)
    expect(component.showLog).toBeTrue()
  })
})
