// noinspection LocalVariableNamingConventionJS
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import { IJobCode } from "../../../metadata/job-code/Jobcode"
import {createMockJobcode} from "../../../../../test/resource/mock-data"
import {
  areSearchResultsEqual,
  isJobCode,
  labelFor,
  searchResultFromString
} from "./form-field-jobcode-search-select.utilities"

describe("form-field-jobcode-search-select.utilities", () => {

  let testJobCode1: IJobCode
  let testJobCode2: IJobCode

  beforeEach(() => {
    testJobCode1 = createMockJobcode(101, "Job Code 1", "1.101")
    testJobCode2 = createMockJobcode(102, "Job Code 2", "1.102")
  })

  it("areSearchResultsEqual should return correct result", () => {
    const copy = createMockJobcode(testJobCode1.targetNode, testJobCode1.targetNodeName, testJobCode1.code)
    expect(areSearchResultsEqual(testJobCode1, copy)).toEqual(true)
    expect(areSearchResultsEqual(testJobCode1, testJobCode2)).toEqual(false)
  })

  it("isJobCode should return correct result", () => {
    expect(isJobCode(testJobCode1)).toEqual(true)
    expect(isJobCode({})).toEqual(false)
    expect(isJobCode(testJobCode1.targetNodeName!!)).toEqual(false)
  })

  it("labelFor should return correct result", () => {
    expect(labelFor(testJobCode1)).toEqual(`${testJobCode1.code}|${testJobCode1.targetNodeName}`)
  })

  it("searchResultFromString should return correct result", () => {
    expect(searchResultFromString(testJobCode1.targetNodeName!!)).toEqual(undefined)
  })
})
