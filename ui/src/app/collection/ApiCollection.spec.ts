import { createMockCollection, createMockCollectionUpdate } from "test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { PublishStatus } from "../PublishStatus"
import { ApiCollection, ApiCollectionUpdate, ICollection, ICollectionUpdate } from "./ApiCollection"


describe("ApiCollection", () => {
  it("ApiCollection should be created", () => {
    // Arrange
    const date = new Date("2020-06-25T14:58:46.313Z")
    const iCollection: ICollection = createMockCollection(date, date, date, date, PublishStatus.Draft)

    // Act
    const apiCollection = new ApiCollection(iCollection)

    // Assert
    expect(apiCollection).toBeTruthy()
    expect(deepEqualSkipOuterType(apiCollection, iCollection)).toBeTruthy(mismatched(apiCollection, iCollection))
  })


  it("ApiCollectionUpdate should be created", () => {
    // Arrange
    const date = new Date("2020-06-25T14:58:46.313Z")
    const iCollectionUpdate: ICollectionUpdate = createMockCollectionUpdate(date, date, date, date, PublishStatus.Draft)

    // Act
    const apiCollectionUpdate = new ApiCollectionUpdate(iCollectionUpdate)

    // Assert
    expect(apiCollectionUpdate).toBeTruthy()
    /* Shallow equal check is good enough here because only 2 fields is non-primitive and is checked separately. */
    /* Cannot do deep check on Interface.  i.e., expect(apiCollectionUpdate).toEqual(iCollectionUpdate) */
    expect(deepEqualSkipOuterType(apiCollectionUpdate, iCollectionUpdate)).toBeTruthy(mismatched(apiCollectionUpdate, iCollectionUpdate))
  })
})
