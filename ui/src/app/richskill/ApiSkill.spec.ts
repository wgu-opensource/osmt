import * as _ from "lodash"
import {
  createMockApiNamedReference,
  createMockAuditLog,
  createMockNamedReference,
  createMockSkill,
  createMockUuidReference
} from "../../../test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { PublishStatus } from "../PublishStatus"
import {
  ApiAuditLog,
  ApiNamedReference,
  ApiSkill,
  ApiUuidReference,
  AuditOperationType,
  IAuditLog,
  INamedReference,
  ISkill,
  IUuidReference
} from "./ApiSkill"

// An example of a class-level test


describe("ApiSkill", () => {
  it("ApiUuidReference should be created", () => {
    // Arrange
    const iUuidReference: IUuidReference = createMockUuidReference()

    // Act
    const apiUuidReference = new ApiUuidReference(iUuidReference)

    // Assert
    expect(apiUuidReference).toBeTruthy()
    expect(deepEqualSkipOuterType(apiUuidReference, iUuidReference)).toBeTruthy(mismatched(apiUuidReference, iUuidReference))
  })


  it("ApiNamedReference should be created", () => {
    // Arrange
    const iNamedReference: INamedReference = createMockNamedReference()

    // Act
    const apiNamedReference = new ApiNamedReference(iNamedReference)

    // Assert
    expect(apiNamedReference).toBeTruthy()
    expect(deepEqualSkipOuterType(apiNamedReference, iNamedReference)).toBeTruthy(mismatched(apiNamedReference, iNamedReference))
  })

  it("ApiNamedReference should be equal", () => {
    // Arrange
    const a = new ApiNamedReference(createMockNamedReference("A", "name"))
    const b = new ApiNamedReference(createMockNamedReference("A", "name"))

    const c = new ApiNamedReference(createMockNamedReference("A"))
    const d = new ApiNamedReference(createMockNamedReference("A", undefined))

    const e = new ApiNamedReference(createMockNamedReference(undefined, undefined))
    const f = new ApiNamedReference(createMockNamedReference())

    // Act
    const result1 = a.equals(b)
    const result2 = c.equals(d)
    const result3 = e.equals(f)

    // Assert
    expect(result1).toBeTruthy()
    expect(result2).toBeTruthy()
    expect(result3).toBeTruthy()
  })

  it("ApiNamedReference should be unequal", () => {
    // None of the following value pairs should match!
    const parameters = [
      ["A", "name1"],
      ["B", "name1"],
      ["B", "name2"],
      ["A", "name2"],
      ["A", undefined],
      [undefined, "name1"],
      [undefined, undefined],
    ]

    for (let i = 0; i < parameters.length; ++i) {
      for (let j = 0; j < parameters.length; ++j) {
        if (i !== j) {
          // Arrange
          const iParam = parameters[i]
          const jParam = parameters[j]
          // WARNING: the createMock... functions use default parameters!!
          const iValue = createMockApiNamedReference(iParam[0], iParam[1])
          const jValue = createMockApiNamedReference(jParam[0], jParam[1])

          // Act
          const result = iValue.equals(jValue)

          // Assert
          expect(result).toBeFalsy(mismatched(iValue, jValue))
        }
      }
    }
  })

  it("ApiNamedReference should be id from text", () => {
    expect(ApiNamedReference.fromString("")).toEqual(undefined)
    expect(ApiNamedReference.fromString("a://b")).toEqual(new ApiNamedReference({ id: "a://b" }))
    expect(ApiNamedReference.fromString("abc")).toEqual(new ApiNamedReference({ name: "abc" }))
  })

  it("ApiNamedReference should be id from text", () => {
    // Arrange
    const a = ApiNamedReference.fromString("my_name")
    const b = new ApiNamedReference({name: "my_name"})  // id field is ignored

    const c = ApiNamedReference.fromString("https://my_id")
    const d = new ApiNamedReference({ id: "https://my_id"})  // name field is ignored

    // Act
    const result1 = a?.equals(b)
    const result2 = c?.equals(d)

    // Assert
    expect(result1).toBeTruthy(mismatched(a, b))
    expect(result2).toBeTruthy(mismatched(c, d))
  })


  it("ApiAuditLog should be created", () => {
    // Arrange
    const iAuditLog: IAuditLog = createMockAuditLog()

    // Act
    const apiAuditLog = new ApiAuditLog(iAuditLog)

    // Assert
    expect(apiAuditLog).toBeTruthy()
    /* cannot do deep equals because the date formats are different (i.e., string != Date) */
    expect(apiAuditLog.creationDate?.toISOString()).toEqual(iAuditLog.creationDate)
    expect(apiAuditLog.operationType).toEqual(iAuditLog.operationType)
    expect(apiAuditLog.user).toEqual(iAuditLog.user)
    expect(_.isEqual(apiAuditLog.changedFields, iAuditLog.changedFields)).toBeTruthy()
  })

  it("ApiAuditLog should match status", () => {
    // Arrange
    const i1 = new ApiAuditLog(createMockAuditLog(AuditOperationType.Insert))
    const i2 = new ApiAuditLog(createMockAuditLog(AuditOperationType.Update))
    const i3 = new ApiAuditLog(createMockAuditLog(AuditOperationType.PublishStatusChange))

    // Act

    // Assert
    expect(i1.isPublishStatusChange()).toBeFalsy()
    expect(i2.isPublishStatusChange()).toBeFalsy()
    expect(i3.isPublishStatusChange()).toBeTruthy()
  })


  it("ApiSkill should be created", () => {
    // Arrange
    const date = new Date("2020-06-25T14:58:46.313Z")
    const iSkill: ISkill = createMockSkill(date, date, PublishStatus.Draft)

    // Act
    const apiSkill = new ApiSkill(iSkill)

    // Assert
    expect(apiSkill).toBeTruthy()
    /* cannot do deep equals because the date formats are different (i.e., string != Date) */
    expect(apiSkill.status).toEqual(iSkill.status)
    expect(apiSkill.id).toEqual(iSkill.id)
    expect(apiSkill.uuid).toEqual(iSkill.uuid)
    expect(apiSkill.creationDate?.toISOString()).toEqual(iSkill.creationDate)  // <--
    expect(apiSkill.updateDate?.toISOString()).toEqual(iSkill.updateDate)      // <--
    expect(apiSkill.type).toEqual(iSkill.type)
    expect(apiSkill.skillName).toEqual(iSkill.skillName)
    expect(apiSkill.skillStatement).toEqual(iSkill.skillStatement)
    expect(apiSkill.categories).toEqual(iSkill.categories)
    expect(apiSkill.collections).toEqual(iSkill.collections)
    expect(apiSkill.keywords).toEqual(iSkill.keywords)
    expect(apiSkill.alignments).toEqual(iSkill.alignments)
    expect(apiSkill.standards).toEqual(iSkill.standards)
    expect(apiSkill.certifications).toEqual(iSkill.certifications)
    expect(apiSkill.occupations).toEqual(iSkill.occupations)
    expect(apiSkill.employers).toEqual(iSkill.employers)
    expect(apiSkill.authors).toEqual(iSkill.authors)
  })
})
