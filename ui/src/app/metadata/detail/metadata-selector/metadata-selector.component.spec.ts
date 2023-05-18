import { ComponentFixture, TestBed } from "@angular/core/testing"
import { MetadataSelectorComponent } from "./metadata-selector.component"

describe("MetadataSelectorComponent", () => {
  let component: MetadataSelectorComponent
  let fixture: ComponentFixture<MetadataSelectorComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSelectorComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataSelectorComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("Should call onValueChange with a param of type string", () => {
    const param = "category"
    const onValueChangeSpy = spyOn(component, "onValueChange").withArgs(param).and.callThrough()

    component.onValueChange("category")

    expect(onValueChangeSpy).toHaveBeenCalled()
    expect(typeof param).toEqual("string")
  })
})
