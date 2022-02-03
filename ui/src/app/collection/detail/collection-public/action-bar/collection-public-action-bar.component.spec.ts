import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { Router } from "@angular/router"
import * as FileSaver from "file-saver"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { createMockTaskResult } from "../../../../../../test/resource/mock-data"
import { CollectionServiceStub } from "../../../../../../test/resource/mock-stubs"
import { ToastService } from "../../../../toast/toast.service"
import { CollectionService } from "../../../service/collection.service"
import { CollectionPublicActionBarComponent } from "./collection-public-action-bar.component"
9

const EXPECTED_URL = "myUrl"
const EXPECTED_UUID = "myId"
const EXPECTED_NAME = "mySkillName"

@Component({
  template: `
    <app-collection-public-action-bar
      [collectionUrl]="collectionUrl"
      [collectionUuid]="collectionUuid"
      [collectionName]="collectionName"
    ></app-collection-public-action-bar>`
})
class TestHostComponent {
  collectionUrl = EXPECTED_URL
  collectionUuid = EXPECTED_UUID
  collectionName = EXPECTED_NAME
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(CollectionPublicActionBarComponent))
  childComponent = debugEl.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  hostFixture.detectChanges()

  return hostFixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    hostFixture.detectChanges()
  })
}


let hostFixture: ComponentFixture<TestHostComponent>
let hostComponent: TestHostComponent
let childComponent: CollectionPublicActionBarComponent


describe("CollectionPublicActionBarComponent", () => {
  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CollectionPublicActionBarComponent,
        TestHostComponent
      ],
      providers: [
        ToastService,
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: Router, useValue: routerSpy }
      ]
    })
    .compileComponents()

    spyOn(FileSaver, "saveAs").and.stub()

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
    expect(childComponent).toBeTruthy()
  })

  it("pollCsv should return", (done) => {
    // Arrange
    childComponent.taskUuidInProgress = "123"
    childComponent.intervalHandle = 1

    // Act
    childComponent.pollCsv()

    // Assert
    /* Delay the handling to give time for the async method to complete. */
    setTimeout(() => {
      expect(childComponent.taskUuidInProgress).toBeFalsy()
      expect(FileSaver.saveAs).toHaveBeenCalled()
      done()
    }, 2000)
  })
  it("pollCsv should fail", (done) => {
    // Arrange
    childComponent.taskUuidInProgress = undefined
    childComponent.intervalHandle = 1

    // Act
    childComponent.pollCsv()

    // Assert
    /* Delay the handling to give time for the async method to complete. */
    setTimeout(() => {
      expect(FileSaver.saveAs).not.toHaveBeenCalled()
      done()
    }, 2000)
  })

  it("onCopyURL should return", () => {
    // Arrange
    const element: HTMLTextAreaElement = document.createElement("TextArea") as HTMLTextAreaElement

    // Act
    childComponent.onCopyURL(element)

    // Assert
    /* Nothing to check */
  })

  it("onCopyJSON should return", () => {
    // Arrange
    const element: HTMLTextAreaElement = document.createElement("TextArea") as HTMLTextAreaElement

    // Act
    childComponent.onCopyJSON(element)

    // Assert
    /* Nothing to check */
  })

  it("onDownloadCsv should return", (done) => {
    // Arrange
    const task = createMockTaskResult()
    childComponent.collectionUuid = "myUUID"
    childComponent.taskUuidInProgress = undefined

    // Act
    childComponent.onDownloadCsv()
    const result = childComponent.taskUuidInProgress

    // Assert
    /* Delay the handling to give time for the async method to complete. */
    setTimeout(() => {
      expect(result === task.uuid).toBeTrue()  // Due to conditional types, cannot use .toEqual()
      expect(FileSaver.saveAs).toHaveBeenCalled()
      done()
    }, 2000)
  })
})
