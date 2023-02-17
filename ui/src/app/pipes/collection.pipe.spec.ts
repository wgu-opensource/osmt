import {CollectionPipe} from "./collection.pipe"
import {PublishStatus} from "../PublishStatus"

describe("CollectionPipe", () => {
  const pipe: CollectionPipe = new CollectionPipe()

  it("should return collection", () => {
    const transform = pipe.transform(PublishStatus.Published)
    expect(transform).toBe("Collection")
  })

  it("should return workspace", () => {
    const transform = pipe.transform(PublishStatus.Workspace)
    expect(transform).toBe("Workspace")
  })

  it("should return my workspace", () => {
    const transform = pipe.transform(PublishStatus.Workspace, true)
    expect(transform).toBe("My Workspace")
  })
})
