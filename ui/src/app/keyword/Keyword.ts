import {IHasUpdateDate, ILongIdDatabaseEntity} from "../ResponseEntity"

export interface Keyword extends ILongIdDatabaseEntity, IHasUpdateDate {
  creationDate: string
  updateDate: string
  value: string,
  type: number,
  uri?: string
}
