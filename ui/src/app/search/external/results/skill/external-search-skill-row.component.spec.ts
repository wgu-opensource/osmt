import { TestBed } from "@angular/core/testing"
import { ExternalSearchSkillRowComponent } from "./external-search-skill-row.component"
import { ApiSkillSearchResult } from "../../api/ApiSkillSearchResult"
import { RichSkillService } from "../../../../richskill/service/rich-skill.service"
import { RichSkillServiceStub } from "../../../../../../test/resource/mock-stubs"
import { ToastService } from "../../../../toast/toast.service"
import { createComponent } from "../../../../../../test/util/test-util.spec"
import { createMockApiSkillSearchResult } from "../../../../../../test/resource/mock-data"

describe("ExternalSearchSkillRowComponent", () => {
  let component: ExternalSearchSkillRowComponent
  let searchResult: ApiSkillSearchResult

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ExternalSearchSkillRowComponent
      ],
      imports: [],
      providers: [
        ToastService,
        { provide: RichSkillService, useClass: RichSkillServiceStub }
      ]
    })
    .compileComponents()

    searchResult = createMockApiSkillSearchResult()

    component = await createComponent(
      ExternalSearchSkillRowComponent,
      (c) => {
        c.searchResult = searchResult
        c.id = "item1"
        c.nextId = "item2"
      }
    )
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should get formatted keywords", () => {
    const expected = searchResult.skill.keywords.join("; ")
    const keywords = component.getFormattedKeywords()
    expect(keywords).toEqual(expected)
  })

  it("should get formatted occupations", () => {
    const expected = searchResult.skill.occupations.filter(o => !!o.detailed).map(o => o.detailed).join("; ")
    const occupations = component.getFormattedOccupations()
    expect(occupations).toEqual(expected)
  })

  it("should import collection", () => {
    spyOn(window, "confirm").and.returnValue(true)
    expect(() => component.onImportSkillClicked()).not.toThrow()
  })
})
