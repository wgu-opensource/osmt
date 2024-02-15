import {TestBed} from "@angular/core/testing"
import {PaginatedSkills} from "../../richskill/service/rich-skill-search.service"
import {RelatedSkillTableControl} from "./related-skill-table.control"
import {RelatedSkillServiceStub} from "../../../../test/resource/mock-stubs"
import {createMockPaginatedSkills} from "../../../../test/resource/mock-data"

class TestControl extends RelatedSkillTableControl<number> {
  execProtected = {
    selectedKeywordFilters: () => this.selectedKeywordFilters,
    setResults: (r: PaginatedSkills | undefined) => this.setResults(r)
  }
}

describe("RelatedSkillTableControl", () => {
  let testControl: TestControl
  let testPaginatedSkills: PaginatedSkills

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        RelatedSkillTableControl,
        TestControl
      ],
      imports: [
      ],
      providers: [
      ]
    }).compileComponents()

    testControl = new TestControl(new RelatedSkillServiceStub())
    testPaginatedSkills = createMockPaginatedSkills()
  })

  it("should be created", () => {
    expect(testControl).toBeTruthy()
  })

  it("get currFirstSkillIndex should return correct result", () => {
    expect(testControl.currFirstSkillIndex).toBeUndefined()

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.currFirstSkillIndex).toEqual(0)
  })

  it("get currLastSkillIndex should return correct result", () => {
    expect(testControl.currLastSkillIndex).toBeUndefined()

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.currLastSkillIndex).toEqual(1)
  })

  it("get currPageCount should return correct result", () => {
    expect(testControl.currPageCount).toBeUndefined()

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.currPageCount).toEqual(1)
  })

  it("get currPageNumber should return correct result", () => {
    expect(testControl.currPageNumber).toEqual(1)

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.currPageNumber).toEqual(1)
  })

  it("get totalCount should return correct result", () => {
    expect(testControl.totalCount).toEqual(0)

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.totalCount).toEqual(testPaginatedSkills.totalCount)
  })

  it("get totalPageCount should return correct result", () => {
    expect(testControl.totalPageCount).toEqual(0)

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.totalPageCount).toEqual(1)
  })

  it("get skills should return correct result", () => {
    expect(testControl.skills).toEqual([])

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.skills.length).toEqual(testPaginatedSkills.skills.length)
  })

  it("get emptyResults should return correct result", () => {
    expect(testControl.emptyResults).toEqual(true)

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.emptyResults).toEqual(false)
  })

  it("get hasResults should return correct result", () => {
    expect(testControl.hasResults).toEqual(false)

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.hasResults).toEqual(true)
  })

  it("get loadingResults should return correct result", () => {
    expect(testControl.loadingResults).toEqual(false)
  })

  it("get selectedKeywordFilters should return correct result", () => {
    expect(testControl.execProtected.selectedKeywordFilters).toBeTruthy()
  })

  it("clearSkills should succeed", () => {
    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.hasResults).toEqual(true)

    testControl.clearSkills()
    expect(testControl.hasResults).toEqual(false)
  })

  it("loadSkills should succeed", () => {
    expect(testControl.hasResults).toEqual(false)

    testControl.loadSkills(1)
    expect(testControl.hasResults).toEqual(true)
  })

  it("setResults should succeed", () => {
    expect(testControl.hasResults).toEqual(false)

    testControl.execProtected.setResults(testPaginatedSkills)
    expect(testControl.hasResults).toEqual(true)

    testControl.execProtected.setResults(undefined)
    expect(testControl.hasResults).toEqual(false)
  })
})
