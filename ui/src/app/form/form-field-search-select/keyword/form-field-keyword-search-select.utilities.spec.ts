// noinspection LocalVariableNamingConventionJS
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {ApiAlignment, ApiNamedReference, IAlignment, INamedReference, KeywordType} from "../../../richskill/ApiSkill"
import {
  areAlignmentsEqual,
  areNamedReferencesEqual,
  areSearchResultsEqual,
  areStringKeywordsEqual,
  isAlignment,
  isAlignmentKeywordType,
  isNamedReference,
  isNamedReferenceKeywordType,
  isStringKeyword,
  isStringKeywordKeywordType,
  labelFor,
  labelForAlignment,
  labelForNamedReference,
  labelForStringKeyword,
  searchResultFromString,
  searchServiceResultToAlignment,
  searchServiceResultToNamedReference,
  searchServiceResultToStringKeyword
} from "./form-field-keyword-search-select.utilities";

describe("form-field-keyword-search-select.utilities", () => {

  let testAlignment1: IAlignment
  let testAlignment2: IAlignment
  let testNamedReference1: INamedReference
  let testNamedReference2: INamedReference
  let testStringKeyword1: string
  let testStringKeyword2: string

  beforeEach(() => {
    testAlignment1 = new ApiAlignment({ skillName: "Alignment 1" })
    testAlignment2 = new ApiAlignment({ skillName: "Alignment 2" })
    testNamedReference1 = new ApiNamedReference({ name: "abc-123" })
    testNamedReference2 = new ApiNamedReference({ name: "321-cba" })
    testStringKeyword1 = "abc-123"
    testStringKeyword2 = "321-cba"
  })

  it("areAlignmentsEqual should return correct result", () => {
    const copy = new ApiAlignment({ id: testAlignment1.id, skillName: testAlignment1.skillName })
    expect(areAlignmentsEqual(testAlignment1, copy)).toEqual(true)
    expect(areAlignmentsEqual(testAlignment1, testAlignment2)).toEqual(false)
  })

  it("isAlignment should return correct result", () => {
    expect(isAlignment(testAlignment1)).toEqual(true)
    expect(isAlignment(testNamedReference1)).toEqual(false)
    expect(isAlignment(testStringKeyword1)).toEqual(false)
  })

  it("isAlignmentKeywordType should return correct result", () => {
    expect(isAlignmentKeywordType(KeywordType.Alignment)).toEqual(true)
    expect(isAlignmentKeywordType(KeywordType.Author)).toEqual(false)
    expect(isAlignmentKeywordType(KeywordType.Category)).toEqual(false)
    expect(isAlignmentKeywordType(KeywordType.Certification)).toEqual(false)
    expect(isAlignmentKeywordType(KeywordType.Employer)).toEqual(false)
    expect(isAlignmentKeywordType(KeywordType.Keyword)).toEqual(false)
    expect(isAlignmentKeywordType(KeywordType.Standard)).toEqual(true)
  })

  it("labelForAlignment should return correct result", () => {
    expect(labelForAlignment(testAlignment1)).toEqual(testAlignment1.skillName!!)
  })

  it("searchServiceResultToAlignment should return correct result", () => {
    expect(searchServiceResultToAlignment({ id: "", name: testAlignment1.skillName })).toEqual(testAlignment1)
    expect(searchServiceResultToAlignment(testAlignment1.skillName)).toEqual(testAlignment1)
  })

  it("areNamedReferencesEqual should return correct result", () => {
    const copy = new ApiNamedReference({ id: testNamedReference1.id, name: testNamedReference1.name })
    expect(areNamedReferencesEqual(testNamedReference1, copy)).toEqual(true)
    expect(areNamedReferencesEqual(testNamedReference1, testNamedReference2)).toEqual(false)
  })

  it("isNamedReference should return correct result", () => {
    expect(isNamedReference(testAlignment1)).toEqual(false)
    expect(isNamedReference(testNamedReference1)).toEqual(true)
    expect(isNamedReference(testStringKeyword1)).toEqual(false)
  })

  it("isNamedReferenceKeywordType should return correct result", () => {
    expect(isNamedReferenceKeywordType(KeywordType.Alignment)).toEqual(false)
    expect(isNamedReferenceKeywordType(KeywordType.Author)).toEqual(false)
    expect(isNamedReferenceKeywordType(KeywordType.Category)).toEqual(false)
    expect(isNamedReferenceKeywordType(KeywordType.Certification)).toEqual(true)
    expect(isNamedReferenceKeywordType(KeywordType.Employer)).toEqual(true)
    expect(isNamedReferenceKeywordType(KeywordType.Keyword)).toEqual(false)
    expect(isNamedReferenceKeywordType(KeywordType.Standard)).toEqual(false)
  })

  it("labelForNamedReference should return correct result", () => {
    expect(labelForNamedReference(testNamedReference1)).toEqual(testNamedReference1.name!!)
  })

  it("searchServiceResultToNamedReference should return correct result", () => {
    expect(searchServiceResultToNamedReference(testNamedReference1)).toEqual(testNamedReference1)
    expect(searchServiceResultToNamedReference(testNamedReference1.name)).toEqual(testNamedReference1)
  })

  it("areStringKeywordsEqual should return correct result", () => {
    const copy = testStringKeyword1
    expect(areStringKeywordsEqual(testStringKeyword1, copy)).toEqual(true)
    expect(areStringKeywordsEqual(testStringKeyword1, testStringKeyword2)).toEqual(false)
  })

  it("isStringKeyword should return correct result", () => {
    expect(isStringKeyword(testAlignment1)).toEqual(false)
    expect(isStringKeyword(testNamedReference1)).toEqual(false)
    expect(isStringKeyword(testStringKeyword1)).toEqual(true)
  })

  it("isStringKeywordType should return correct result", () => {
    expect(isStringKeywordKeywordType(KeywordType.Alignment)).toEqual(false)
    expect(isStringKeywordKeywordType(KeywordType.Author)).toEqual(true)
    expect(isStringKeywordKeywordType(KeywordType.Category)).toEqual(true)
    expect(isStringKeywordKeywordType(KeywordType.Certification)).toEqual(false)
    expect(isStringKeywordKeywordType(KeywordType.Employer)).toEqual(false)
    expect(isStringKeywordKeywordType(KeywordType.Keyword)).toEqual(true)
    expect(isStringKeywordKeywordType(KeywordType.Standard)).toEqual(false)
  })

  it("labelForStringKeyword should return correct result", () => {
    expect(labelForStringKeyword(testStringKeyword1)).toEqual(testStringKeyword1)
  })

  it("searchServiceResultToStringKeyword should return correct result", () => {
    expect(searchServiceResultToStringKeyword({ id: "", name: testStringKeyword1 })).toEqual(testStringKeyword1)
    expect(searchServiceResultToStringKeyword(testStringKeyword1)).toEqual(testStringKeyword1)
  })

  it("areSearchResultsEqual should return correct result", () => {
    expect(areSearchResultsEqual(testAlignment1, testAlignment1)).toEqual(true)
    expect(areSearchResultsEqual(testAlignment1, testNamedReference1)).toEqual(false)
    expect(areSearchResultsEqual(testAlignment1, testStringKeyword1)).toEqual(false)
    expect(areSearchResultsEqual(testNamedReference1, testAlignment1)).toEqual(false)
    expect(areSearchResultsEqual(testNamedReference1, testNamedReference1)).toEqual(true)
    expect(areSearchResultsEqual(testNamedReference1, testStringKeyword1)).toEqual(false)
    expect(areSearchResultsEqual(testStringKeyword1, testAlignment1)).toEqual(false)
    expect(areSearchResultsEqual(testStringKeyword1, testNamedReference1)).toEqual(false)
    expect(areSearchResultsEqual(testStringKeyword1, testStringKeyword1)).toEqual(true)
  })

  it("labelFor should return correct result", () => {
    expect(labelFor(testAlignment1)).toEqual(testAlignment1.skillName!!)
    expect(labelFor(testNamedReference1)).toEqual(testNamedReference1.name!!)
    expect(labelFor(testStringKeyword1)).toEqual(testStringKeyword1)
  })

  it("searchResultFromString should return correct result", () => {
    expect(searchResultFromString(KeywordType.Alignment, testAlignment1.skillName!!)).toEqual(testAlignment1)
    expect(searchResultFromString(KeywordType.Author, testStringKeyword1)).toEqual(testStringKeyword1)
    expect(searchResultFromString(KeywordType.Category, testNamedReference1.name!!)).toEqual(testStringKeyword1)
    expect(searchResultFromString(KeywordType.Certification, testNamedReference1.name!!)).toEqual(testNamedReference1)
    expect(searchResultFromString(KeywordType.Employer, testNamedReference1.name!!)).toEqual(testNamedReference1)
    expect(searchResultFromString(KeywordType.Keyword, testStringKeyword1)).toEqual(testStringKeyword1)
    expect(searchResultFromString(KeywordType.Standard, testAlignment1.skillName!!)).toEqual(testAlignment1)
  })
})
