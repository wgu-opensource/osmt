import { HttpClientTestingModule } from "@angular/common/http/testing"
import { ComponentFixture, TestBed } from "@angular/core/testing"

import { ExportCollectionComponent } from "./export-collection.component"
import { CollectionService } from "../collection/service/collection.service"
import { CollectionServiceStub } from "../../../test/resource/mock-stubs"

describe("ExportCollectionComponent", () => {
  let component: ExportCollectionComponent
  let fixture: ComponentFixture<ExportCollectionComponent>
  let collectionService: CollectionService

  beforeEach(async () => {
    TestBed.configureTestingModule({
      declarations: [
        ExportCollectionComponent
      ],
      providers: [
        { provide: CollectionService, useClass: CollectionServiceStub },
      ],
      imports: [
        HttpClientTestingModule
      ]
    })
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(ExportCollectionComponent)
    collectionService = TestBed.inject(CollectionService)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("getCollectionCsv should work", () => {
    const uuid = "d18ef6a0-e8f6-49b1-92bb-eadfaef6b1a9"
    const entityName = "Collection name"
    const spyLoader = spyOn(component["toastService"], "showBlockingLoader")
    const spyService = spyOn(collectionService, "requestCollectionSkillsCsv").and.callThrough()
    component.getCollectionCsv(uuid, entityName)
    expect(spyLoader)
    expect(spyService).toHaveBeenCalled()
  })

  it("getCollectionXlsx should work", () => {
    const uuid = "d18ef6a0-e8f6-49b1-92bb-eadfaef6b1a9"
    const entityName = "Collection name"
    const spyLoader = spyOn(component["toastService"], "showBlockingLoader")
    const spyService = spyOn(collectionService, "requestCollectionSkillsXlsx").and.callThrough()
    component.getCollectionXlsx(uuid, entityName)
    expect(spyLoader)
    expect(spyService).toHaveBeenCalled()
  })

  it("pollCsv should return", () => {
    component.taskUuidInProgress = undefined
    const spy = spyOn(collectionService, "getCsvTaskResultsIfComplete").and.callThrough()
    component.pollCsv()
    expect(spy).not.toHaveBeenCalled()
  })

  it("pollCsv should call getCsvTaskResultIfComplete", () => {
    component.taskUuidInProgress = "345-dfh-23421a-as3423"
    const spy = spyOn(collectionService, "getCsvTaskResultsIfComplete").and.callThrough()
    component.pollCsv()
    expect(spy).toHaveBeenCalled()
  })

  it("pollXlsx should return", () => {
    component.taskUuidInProgress = undefined
    const spy = spyOn(collectionService, "getXlsxTaskResultsIfComplete").and.callThrough()
    component.pollXlsx()
    expect(spy).not.toHaveBeenCalled()
  })

  it("pollXlsx should call getCsvTaskResultIfComplete", () => {
    component.taskUuidInProgress = "345-dfh-23421a-as3423"
    const spy = spyOn(collectionService, "getXlsxTaskResultsIfComplete").and.callThrough()
    component.pollXlsx()
    expect(spy).toHaveBeenCalled()
  })
})
