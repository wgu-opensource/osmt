// tslint:disable-next-line:no-empty-interface
export interface IResponseEntity {
}

export interface IHasUpdateDate {
  updateDate: string
}

export interface IUuidDatabaseEntity extends IResponseEntity {
  id: string
}

export interface ILongIdDatabaseEntity extends IResponseEntity {
  id: number
}
