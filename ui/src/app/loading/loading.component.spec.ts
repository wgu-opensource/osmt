import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { LoadingComponent } from "./loading.component"

// An example of how to test an @Input


const EXPECTED_CLASS_NAME = "foo"

@Component({
  template: `
    <app-loading [className]="myClass">
    </app-loading>`
})
class TestHostComponent {
  myClass = EXPECTED_CLASS_NAME
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(LoadingComponent))
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
let childComponent: LoadingComponent


describe("LoadingComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        LoadingComponent,
        TestHostComponent
      ]
    })
    .compileComponents()

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
    expect(childComponent).toBeTruthy()
  })

  it("should assign className correctly", () => {
    expect(childComponent.className).toEqual(EXPECTED_CLASS_NAME)
  })
})
