import {
  createMockSkillUpdate,
  createMockApiReferenceListUpdate,
  createMockStringListUpdate
} from "test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { ApiNamedReference } from "./ApiSkill"
import {
  ApiSkillUpdate,
  ApiReferenceListUpdate,
  IRichSkillUpdate,
  IReferenceListUpdate,
  IStringListUpdate, ApiStringListUpdate
} from "./ApiSkillUpdate"


describe("ApiSkillUpdate", () => {
  it("ApiSkillUpdate should be created", () => {
    // Arrange
    const iSkillUpdate: IRichSkillUpdate = createMockSkillUpdate()

    // Act
    const apiSkillUpdate = new ApiSkillUpdate(iSkillUpdate)

    // Assert
    expect(apiSkillUpdate).toBeTruthy()
    expect(deepEqualSkipOuterType(apiSkillUpdate, iSkillUpdate)).toBeTruthy(mismatched(apiSkillUpdate, iSkillUpdate))
  })


  it("ApiStringListUpdate should be created", () => {
    // Arrange
    const iStringListUpdate: IStringListUpdate = createMockStringListUpdate()

    // Act
    const apiStringListUpdate = new ApiStringListUpdate(iStringListUpdate.add, iStringListUpdate.remove)

    // Assert
    expect(apiStringListUpdate).toBeTruthy()
    expect(deepEqualSkipOuterType(apiStringListUpdate, iStringListUpdate)).toBeTruthy(mismatched(apiStringListUpdate, iStringListUpdate))
  })


  it("ApiReferenceListUpdate should be created", () => {
    // Arrange
    const ref: ApiReferenceListUpdate = createMockApiReferenceListUpdate()

    // Act
    const apiReferenceListUpdate = new ApiReferenceListUpdate(ref.add, ref.remove)

    // Assert
    expect(apiReferenceListUpdate).toBeTruthy()
    expect(deepEqualSkipOuterType(apiReferenceListUpdate, ref)).toBeTruthy(mismatched(apiReferenceListUpdate, ref))
  })
})
