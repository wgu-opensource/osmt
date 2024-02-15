import { ComponentFixture, TestBed } from "@angular/core/testing"

import { DropUpMenuComponent } from "./drop-up-menu.component"

describe("DropUpMenuComponent", () => {
  let component: DropUpMenuComponent
  let fixture: ComponentFixture<DropUpMenuComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DropUpMenuComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(DropUpMenuComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
