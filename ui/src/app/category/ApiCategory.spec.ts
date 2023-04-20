import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {ApiCategory, IKeyword} from "./ApiCategory"
import {KeywordType} from "../richskill/ApiSkill"

describe("ApiCategory", () => {
  it("ApiCategory should be created", () => {
    // Arrange
    const iKeyword: IKeyword = {
      type: KeywordType.Category,
      id: 1,
      value: "category1",
      skillCount: 10
    }

    // Act
    const apiCategory = new ApiCategory(iKeyword)

    // Assert
    expect(apiCategory).toBeTruthy()
    expect(apiCategory.id).toEqual(iKeyword.id)
    expect(apiCategory.name).toEqual(iKeyword.value)
    expect(apiCategory.skillCount).toEqual(iKeyword.skillCount)
  })
})
