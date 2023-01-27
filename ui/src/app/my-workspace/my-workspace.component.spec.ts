import { ComponentFixture, TestBed } from "@angular/core/testing"

import { MyWorkspaceComponent } from "./my-workspace.component"

describe("MyWorkspaceComponent", () => {
  let component: MyWorkspaceComponent
  let fixture: ComponentFixture<MyWorkspaceComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyWorkspaceComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(MyWorkspaceComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
