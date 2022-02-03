import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { ApiSkillSummary } from "../../richskill/ApiSkillSummary"
import { DotsMenuComponent } from "./dots-menu.component"
import { TableActionDefinition } from "./has-action-definitions"


const EXPECTED_CLASS_NAME = "foo"

@Component({
  template: `
    <app-dots-menu [actions]="rowActions" [data]="skill"></app-dots-menu>`
})
class TestHostComponent {
  myClass = EXPECTED_CLASS_NAME
  rowActions: TableActionDefinition[] = []
  skill: ApiSkillSummary | null = null
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(DotsMenuComponent))
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
let childComponent: DotsMenuComponent


describe("DotsMenuComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        DotsMenuComponent,
        TestHostComponent
      ]
    })
    .compileComponents()

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
  })

  it("handleClickTrigger should return", () => {
    // Arrange

    // Act
    const result = childComponent.handleClickTrigger()

    // Assert
    expect(result).toBeFalse()
  })

  it("handleClickAction should return", () => {
    // Arrange
    const action = new TableActionDefinition({
      label: "Add to Collection",
      callback: undefined
    })

    // Act
    const result = childComponent.handleClickAction(action)

    // Assert
    expect(result).toBeFalse()
  })

  it("toggle should open", () => {
    // Arrange
    childComponent.isOpen = false

    // Act
    childComponent.toggle()

    // Assert
    expect(childComponent.isOpen).toBeTrue()
  })
  it("toggle should close", () => {
    // Arrange
    childComponent.isOpen = true

    // Act
    childComponent.toggle()

    // Assert
    expect(childComponent.isOpen).toBeFalse()
  })
})
