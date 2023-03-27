import { ComponentFixture, TestBed } from "@angular/core/testing"

import { SizePaginationComponent } from "./size-pagination.component"

describe("SizePaginationComponent", () => {
  let component: SizePaginationComponent
  let fixture: ComponentFixture<SizePaginationComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SizePaginationComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(SizePaginationComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
