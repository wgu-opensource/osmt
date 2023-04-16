import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {Injectable} from "@angular/core";
import {AbstractPillControl, KeywordCountPillControl} from "./pill-control";
import {KeywordCount} from "../../richskill/ApiSkill";

@Injectable({
  providedIn: 'root',
})
export class TestPillControl extends AbstractPillControl {
  static readonly TEST_PRIMARY_STRING = "string1"
  static readonly  TEST_SECONDARY_STRING = "string2"
  readonly hasSecondaryValue: boolean = true

  get primaryLabel(): string {
    return TestPillControl.TEST_PRIMARY_STRING
  }

  get secondaryLabel(): string | undefined {
    return (this.hasSecondaryValue) ? TestPillControl.TEST_SECONDARY_STRING : undefined
  }
}

@Injectable({
  providedIn: 'root',
})
export class TestPrimaryOnlyPillControl extends TestPillControl {
  readonly hasSecondaryValue: boolean = false
}

describe("AbstractPillControl", () => {

  let testControl: AbstractPillControl
  let testPrimaryOnlyControl: AbstractPillControl

  beforeEach(() => {
    testControl = new TestPillControl()
    testPrimaryOnlyControl = new TestPrimaryOnlyPillControl()
  })

  it("should be created", () => {
    expect(testControl).toBeTruthy()
    expect(testPrimaryOnlyControl).toBeTruthy()
  })

  it("get secondaryLabel should return correct result", () => {
    expect(testControl.secondaryLabel).toEqual(TestPillControl.TEST_SECONDARY_STRING)
    expect(testPrimaryOnlyControl.secondaryLabel).toEqual(undefined)
  })

  it("get isSelected should return correct result", () => {
    expect(testControl.isSelected).toEqual(false)
    testControl.select()
    expect(testControl.isSelected).toEqual(true)
    testControl.deselect()
    expect(testControl.isSelected).toEqual(false)
  })

  it("deselect should set succeed", () => {
    testControl.select()
    expect(testControl.isSelected).toEqual(true)
    testControl.deselect()
    expect(testControl.isSelected).toEqual(false)
  })

  it("select should set succeed", () => {
    expect(testControl.isSelected).toEqual(false)
    testControl.select()
    expect(testControl.isSelected).toEqual(true)
  })
})

describe("KeywordCountPillControl", () => {

  const testKwCount: KeywordCount = new KeywordCount({ keyword: "test", count: 5 })
  let testControl: KeywordCountPillControl

  beforeEach(() => {
    testControl = new KeywordCountPillControl(testKwCount)
  })

  it("should be created", () => {
    expect(testControl).toBeTruthy()
  })

  it("get secondaryLabel should return correct result", () => {
    expect(testControl.keyword).toEqual(testKwCount.keyword)
  })

  it("get secondaryLabel should return correct result", () => {
    expect(testControl.count).toEqual(testKwCount.count)
  })

  it("get secondaryLabel should return correct result", () => {
    expect(testControl.primaryLabel).toEqual(`${testKwCount.keyword}`)
  })

  it("get secondaryLabel should return correct result", () => {
    expect(testControl.secondaryLabel).toEqual(`${testKwCount.count}`)
  })
})
