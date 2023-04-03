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

  it("Should call onValueChange with a param of type number", () => {
    const param = 100
    const onValueChangeSpy = spyOn(component, "onValueChange").withArgs(param).and.callThrough()

    component.onValueChange(100);

    expect(component.values).toContain(param)
    expect(onValueChangeSpy).toHaveBeenCalled()
    expect(typeof param).toEqual('number')
  })
})
