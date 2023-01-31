import {ApiCollectionSummary, ApiSkillSummary} from "./richskill/ApiSkillSummary";

export enum PublishStatus {
  Unarchived = "Unarchived",
  Published = "Published",
  Archived = "Archived",
  Deleted = "Deleted",
  Draft = "Draft",
  Workspace = "Workspace"
}


export function determineFilters(selectedFilters: Set<PublishStatus>): Set<PublishStatus> {
  // build a bitfield from Draft | Published | Archived
  const value = (selectedFilters.has(PublishStatus.Draft) ? 1 : 0) << 2 |
    (selectedFilters.has(PublishStatus.Published) ? 1 : 0) << 1 |
    (selectedFilters.has(PublishStatus.Archived) ? 1 : 0) << 0

  // truth table for determining which statuses to send to API
  // Draft|Published|Archived -> Set<PublishStatus>
  const truthTable = {
    0b000: [],
    0b001: [PublishStatus.Archived, PublishStatus.Deleted],
    0b010: [PublishStatus.Published],
    0b011: [PublishStatus.Published, PublishStatus.Archived],
    0b100: [PublishStatus.Draft],
    0b101: [PublishStatus.Draft, PublishStatus.Deleted],
    0b110: [PublishStatus.Draft, PublishStatus.Published],
    0b111: [PublishStatus.Draft, PublishStatus.Published, PublishStatus.Archived, PublishStatus.Deleted],
  }

  // @ts-ignore
  return new Set(truthTable[value] ?? [])
}

export function checkArchived(i: ApiSkillSummary | ApiCollectionSummary): boolean {
  return i.status === PublishStatus.Archived || i.status === PublishStatus.Deleted
}

