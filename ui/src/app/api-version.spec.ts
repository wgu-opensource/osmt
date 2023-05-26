import { ApiVersion, getBaseApi } from "./api-versions"

describe("api version", () => {

  it("current version used is v3", () => {
    const currentVersionIsV3 = getBaseApi().includes("v3")
    expect(currentVersionIsV3).toBeTrue()
  })

  it("getBaseApi should not return version", () => {
    const baseApi = getBaseApi(ApiVersion.API_V2)
    expect(baseApi).toBe("/api")
  })

  it("getBaseApi should return version", () => {
    const baseApi = getBaseApi()
    expect(baseApi).toBe("/api/v3")
  })

})
