import { TestBed } from "@angular/core/testing"
import { ExternalSearchSkillTableComponent } from "./external-search-skill-table.component"
import { createComponent } from "../../../../../../test/util/test-util.spec"

describe("ExternalSearchSkillTableComponent", () => {
  let component: ExternalSearchSkillTableComponent

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ExternalSearchSkillTableComponent
      ],
      imports: [],
      providers: []
    })
    .compileComponents()

    component = await createComponent(ExternalSearchSkillTableComponent)
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })
})
