import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {RichSkillsCsvExportComponent} from "./rich-skills-csv-export.component"


describe("RichSkillCsvExport", () => {
  let component: RichSkillsCsvExportComponent
  let fixture: ComponentFixture<RichSkillsCsvExportComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichSkillsCsvExportComponent]
    })
      .compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichSkillsCsvExportComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
