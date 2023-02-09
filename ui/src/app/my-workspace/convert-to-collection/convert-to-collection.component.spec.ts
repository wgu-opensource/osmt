import { ComponentFixture, TestBed } from "@angular/core/testing"

import { ConvertToCollectionComponent } from "./convert-to-collection.component"

describe("ConvertToCollectionComponent", () => {
  let component: ConvertToCollectionComponent
  let fixture: ComponentFixture<ConvertToCollectionComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConvertToCollectionComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(ConvertToCollectionComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
