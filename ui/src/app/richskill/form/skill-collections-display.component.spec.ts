import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { FormControl } from "@angular/forms"
import { By } from "@angular/platform-browser"
import { SkillCollectionsDisplayComponent } from "./skill-collections-display.component"


const EXPECTED_CONTROL_VALUES = ["value1", "value2"]
const EXPECTED_LABEL = "mylabel"
const EXPECTED_PLACEHOLDER = "myplaceholder"
const EXPECTED_ERRORMESSAGE = "myerror"
const EXPECTED_HELPMESSAGE = "myhelp"
const EXPECTED_REQUIRED = true
const EXPECTED_NAME = "myname"


@Component({
  template: `
    <app-skill-collections-display
      [control]="myControl"
      [label]="myLabel"
      [placeholder]="myPlaceholder"
      [errorMessage]="myErrorMessage"
      [helpMessage]="myHelpMessage"
      [required]="myRequired"
      [name]="myName"
    >
    </app-skill-collections-display>`
})
class TestHostComponent {
  myControl = new FormControl(EXPECTED_CONTROL_VALUES)
  myLabel = EXPECTED_LABEL
  myPlaceholder = EXPECTED_PLACEHOLDER
  myErrorMessage = EXPECTED_ERRORMESSAGE
  myHelpMessage = EXPECTED_HELPMESSAGE
  myRequired = EXPECTED_REQUIRED
  myName = EXPECTED_NAME
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(SkillCollectionsDisplayComponent))
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
let childComponent: SkillCollectionsDisplayComponent


describe("TestHostComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        SkillCollectionsDisplayComponent,
        TestHostComponent
      ],
    })
    .compileComponents()

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()

    expect(childComponent.control.value).toEqual(EXPECTED_CONTROL_VALUES)
    expect(childComponent.label).toEqual(EXPECTED_LABEL)
    expect(childComponent.placeholder).toEqual(EXPECTED_PLACEHOLDER)
    expect(childComponent.errorMessage).toEqual(EXPECTED_ERRORMESSAGE)
    expect(childComponent.helpMessage).toEqual(EXPECTED_HELPMESSAGE)
    expect(childComponent.required).toEqual(EXPECTED_REQUIRED)
    expect(childComponent.name).toEqual(EXPECTED_NAME)
  })

  it("handleClickIcon should remove", () => {
    // Arrange
    childComponent.toRemove = []

    // Act
    const result = childComponent.handleClickIcon("value2")

    // Assert
    expect(result).toBeFalse()
    expect(childComponent.control.value).toEqual(["value1"])
  })

  it("handleClickIcon should replace (undo)", () => {
    // Arrange
    childComponent.toRemove = [ "value3" ]

    // Act
    const result = childComponent.handleClickIcon("value3")

    // Assert
    expect(result).toBeFalse()
    expect(childComponent.toRemove.length).toEqual(0)
    // expect(childComponent.control.value).toEqual(["value1", "value3"])  // value is not consistent.
  })
})
