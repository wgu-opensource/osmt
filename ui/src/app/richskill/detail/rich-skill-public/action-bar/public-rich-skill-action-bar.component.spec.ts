import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { Router } from "@angular/router"
import * as FileSaver from "file-saver"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { RichSkillServiceStub } from "../../../../../../test/resource/mock-stubs"
import { ToastService } from "../../../../toast/toast.service"
import { RichSkillService } from "../../../service/rich-skill.service"
import { PublicRichSkillActionBarComponent } from "./public-rich-skill-action-bar.component"


const EXPECTED_URL = "myUrl"
const EXPECTED_UUID = "myId"
const EXPECTED_NAME = "myCollectionName"

@Component({
  template: `
    <app-abstract-public-rich-skill-action-bar
      [skillUrl]="skillUrl"
      [skillUuid]="skillUuid"
      [skillName]="skillName"
    ></app-abstract-public-rich-skill-action-bar>`
})
class TestHostComponent {
  skillUrl = EXPECTED_URL
  skillUuid = EXPECTED_UUID
  skillName = EXPECTED_NAME
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(PublicRichSkillActionBarComponent))
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
let childComponent: PublicRichSkillActionBarComponent


describe("PublicRichSkillActionBarComponent", () => {
  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        PublicRichSkillActionBarComponent,
        TestHostComponent
      ],
      providers: [
        ToastService,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
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

  it("getRsdCsv should return", () => {
    // Arrange
    childComponent.skillUuid = "myUUID"
    childComponent.skillName = "myName"

    // Act
    childComponent.exporter.getRsdCsv(
      childComponent.skillUuid,
      childComponent.skillName
    )

    // Assert
    expect(FileSaver.saveAs).toHaveBeenCalled()
  })
})
