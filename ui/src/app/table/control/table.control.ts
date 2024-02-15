import {ApiSortOrder} from "../../richskill/ApiSkill"
import {PublishStatus} from "../../PublishStatus"

export interface ITableControl {
  from?: number
  size?: number
}

export interface ISkillTableControl extends ITableControl {
  sort?: ApiSortOrder | undefined
  query?: string | undefined
  statusFilters?: Set<PublishStatus>
}
