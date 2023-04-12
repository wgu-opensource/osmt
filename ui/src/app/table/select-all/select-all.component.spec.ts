import { ComponentFixture, TestBed } from "@angular/core/testing"

import {SelectAll, SelectAllComponent} from "./select-all.component"

describe("SelectAllComponent", () => {
  let component: SelectAllComponent
  let fixture: ComponentFixture<SelectAllComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectAllComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectAllComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("on click checkbox should emit", () => {
    const spyEmit = spyOn(component.valueChange, "emit")
    const event = {target: {checked: true}}
    component.onClickCheckbox(event as any)
    expect(spyEmit).toHaveBeenCalled()
  })

  it("on click checkbox should emit value 0", () => {
    const spyEmit = spyOn(component.valueChange, "emit")
    const event = {target: {checked: true}}
    component.select = {nativeElement: {value: SelectAll.SELECT_ALL}}
    component.onClickCheckbox(true)
    expect(spyEmit).toHaveBeenCalledWith({value: 0, selected: true})
  })

})
