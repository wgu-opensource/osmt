import { ICollection, ICollectionUpdate } from "../../src/app/collection/ApiCollection"
import { IJobCode } from "../../src/app/job-codes/Jobcode"
import { PublishStatus } from "../../src/app/PublishStatus"
import { IBatchResult } from "../../src/app/richskill/ApiBatchResult"
import {
  ApiNamedReference,
  AuditOperationType,
  IAuditLog,
  INamedReference,
  ISkill,
  IUuidReference
} from "../../src/app/richskill/ApiSkill"
import {
  ApiCollectionSummary,
  ICollectionSummary,
  ISkillSummary
} from "../../src/app/richskill/ApiSkillSummary"
import { ApiReferenceListUpdate, IRichSkillUpdate, IStringListUpdate } from "../../src/app/richskill/ApiSkillUpdate"
import { PaginatedCollections, PaginatedSkills } from "../../src/app/richskill/service/rich-skill-search.service"
import { ITaskResult } from "../../src/app/task/ApiTaskResult"

// Add mock data here.
// For more examples, see https://github.com/WGU-edu/ema-eval-ui/blob/develop/src/app/admin/pages/edit-user/edit-user.component.spec.ts


export function createMockBatchResult(): IBatchResult {
  return {
    message: "my batchResults message",
    modifiedCount: 42,
    totalCount: 103,
    success: true
  }
}

export function createMockJobcode(): IJobCode {
  return {
    id: 42,
    name: "my jobcode name",
    code: "my jobcode",
    broad: "my jobcode broad",
    broadCode: "my jobcode broadCode",
    detailed: "my jobcode detailed",
    level: "Broad",
    major: "my jobcode major",
    majorCode: "my jobcode majorCode",
    framework: "my jobcode framework",
    minor: "my jobcode minor",
    minorCode: "my jobcode minorCode",
    url: "my jobcode url",
    parents: undefined
  }
}

export function createMockUuidReference(uuid = "my uuidReference id", name = "my uuidReference name"): IUuidReference {
  return {
    uuid,
    name
  }
}

export function createMockNamedReference(id = "id", name = "name"): INamedReference {
  return {
    id,
    name
  }
}

export function createMockApiNamedReference(id?: string, name?: string): ApiNamedReference {
  if (id && name) {
    return new ApiNamedReference({ id, name })
  }
  if (id) {
    return new ApiNamedReference({ id })
  }
  if (name) {
    return new ApiNamedReference({ name })
  }
  return new ApiNamedReference({ })
}

export function createMockAuditLog(operationType = AuditOperationType.Insert): IAuditLog {
  return {
    creationDate: "2020-06-25T14:58:46.313Z",
    operationType,
    user: "my user",
    changedFields: [
      {
        fieldName: "my field name",
        old: "old value",
        new: "new value"
      },
    ]
  }
}

export function createMockStringListUpdate(): IStringListUpdate {
  return {
    add: [ "string 1", "string 2" ],
    remove: [ "string 3" ]
  }
}

export function createMockApiReferenceListUpdate(): ApiReferenceListUpdate {
  return {
    add: [
      new ApiNamedReference(createMockNamedReference(  "1", "one")),
      new ApiNamedReference(createMockNamedReference(  "2", "two")),
    ],
    remove: [
      new ApiNamedReference(createMockNamedReference(  "3", "three")),
    ]
  }
}

export function createMockSkillUpdate(): IRichSkillUpdate {
  return {
    skillName: "my skill name",
    skillStatement: "my skill statement",
    status: PublishStatus.Draft,
    category: "my skill category",
    keywords: createMockStringListUpdate(),
    collections: createMockStringListUpdate(),
    alignments: createMockApiReferenceListUpdate(),
    certifications: createMockApiReferenceListUpdate(),
    standards: createMockApiReferenceListUpdate(),
    occupations: createMockStringListUpdate(),
    employers: createMockApiReferenceListUpdate(),
    author: new ApiNamedReference(createMockNamedReference("a", "author"))
  }
}

export function createMockSkillSummary(
  id = "id1",
  status = PublishStatus.Draft,
  publishDate = "2020-06-25T14:58:46.313Z"
): ISkillSummary {
  return {
    id,
    uuid: "uu" + id,
    status,
    archiveDate: "2020-06-25T14:58:46.313Z",
    publishDate: publishDate ? publishDate : undefined,  // i.e., if "" is passed in, then treat as undefined here.
    skillName: "my skill summary name",
    skillStatement: "my skill summary statement",
    category: "my skill category",
    keywords: [ "keyword 1", "keyword 2" ],
    occupations: [ createMockJobcode(), createMockJobcode() ]
  }
}
export function createMockPaginatedSkills(skillCount = 1, total = 10): PaginatedSkills {
  if (skillCount > total) {
    throw new RangeError(`'pageCount' must be <= 'total'`)
  }

  const skills = []
  for (let c = 1; c <= skillCount; ++c) {
    skills.push(
      createMockSkillSummary(
        "id" + c,
        PublishStatus.Draft,
        "2020-06-25T14:58:46.313Z"
      )
    )
  }

  return new PaginatedSkills(
    skills,
    total
  )
}

export function createMockCollectionSummary(
  id = "id1",
  status = PublishStatus.Draft,
  publishDate = "2020-06-25T14:58:46.313Z"
): ICollectionSummary {
  return {
    id,
    uuid: "my collection summary uuid",
    name: "my collection summary name",
    skillCount: 42,
    status,
    archiveDate: "2020-06-25T14:58:46.313Z",
    publishDate: publishDate ? publishDate : undefined  // i.e., if "" is passed in, then treat as undefined here.
  }
}
export function createMockPaginatedCollections(collectionCount = 1, total = 10): PaginatedCollections {
  if (collectionCount > total) {
    throw new RangeError(`'pageCount' must be <= 'total'`)
  }

  const collections = []
  for (let c = 1; c <= collectionCount; ++c) {
    collections.push(
      new ApiCollectionSummary(createMockCollectionSummary(
        "id" + c,
        PublishStatus.Draft,
        "2020-06-25T14:58:46.313Z"
      ))
    )
  }

  return new PaginatedCollections(
    collections,
    total
  )
}

export function createMockTaskResult(): ITaskResult {
  return {
    status: PublishStatus.Draft,
    contentType: "my content type",
    id: "my collection summary id",
    uuid: "my collection summary uuid"
  }
}

export function createMockSkill(creationDate: Date, updateDate: Date, status: PublishStatus): ISkill {
  return {
    creationDate: creationDate.toISOString(),
    updateDate: updateDate.toISOString(),
    id: "my skill id",
    uuid: "my skill uuid",
    type: "some type",
    skillName: "my skill name",
    skillStatement: "my skill statement",
    status,
    category: "my skill category",
    collections: [createMockUuidReference()],
    keywords: ["keyword 1", "keyword 2"],
    alignments: [createMockNamedReference()],
    standards: [createMockNamedReference()],
    certifications: [createMockNamedReference()],
    occupations: [createMockJobcode()],
    employers: [createMockNamedReference()],
    author: createMockNamedReference()
  }
}

export function createMockCollection(
  creationDate: Date | undefined,
  updateDate: Date | undefined,
  archiveDate: Date | undefined,
  publishDate: Date | undefined,
  status: PublishStatus,
  skills: string[] = ["skill 1", "skill 2"]
): ICollection {
  return {
    creationDate,
    updateDate,
    archiveDate,
    publishDate,
    status,
    id: "id1",
    uuid: "uuid1",
    name: "my collection name",
    author: createMockNamedReference(),
    skills,
    creator: "creator"
  }
}

export function createMockCollectionUpdate(creationDate: Date, updateDate: Date, archiveDate: Date, publishDate: Date,
                                           status: PublishStatus): ICollectionUpdate {
  return {
    status,
    name: "my collection name",
    author: createMockNamedReference(),
    skills: createMockStringListUpdate()
  }
}
