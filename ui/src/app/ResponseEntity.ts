export interface IResponseEntity {
  creationDate: string
}

export interface IHasUpdateDate extends IResponseEntity {
  updateDate: string,
}

export interface IUuidDatabaseEntity extends IHasUpdateDate {
  uuid: string
}

export interface ILongIdDatabaseEntity extends IResponseEntity {
  id: number
}
