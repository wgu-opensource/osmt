import { FilterSearchComponent } from "./filter-search.component"
import {ApiNamedReference} from "../../richskill/ApiSkill"
import {ApiJobCode} from "../../metadata/job-codes/Jobcode"

describe("FilterSearchComponent", () => {

  const apiNameReferenced1 = new ApiNamedReference({id: "1", name: "value1"})
  const apiNameReferenced2 = new ApiNamedReference({id: "2", name: "value2"})
  const filterSearch = new FilterSearchComponent()


  it("are results equals should be true", () => {
    const areEqual = filterSearch.areResultsEqual(apiNameReferenced1, apiNameReferenced1)
    expect(areEqual).toBeTrue()
  })

  it("are results equals should be false", () => {
    const areEqual = new FilterSearchComponent().areResultsEqual(apiNameReferenced1, apiNameReferenced2)
    expect(areEqual).toBeFalse()
  })

  it("name should be correct for api named reference", () => {
    const name = filterSearch.resultName(apiNameReferenced1)
    expect(name).toEqual("value1")
  })

  it("name should be correct for api job code", () => {
    const name = filterSearch.resultName(new ApiJobCode({code: "code1", targetNodeName: "value1"}))
    expect(name).toEqual("code1 value1")
  })
})
