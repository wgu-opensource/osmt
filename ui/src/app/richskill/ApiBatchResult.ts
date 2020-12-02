


export interface IBatchResult {
  success?: boolean;
  message?: string;
  modifiedCount?: number;
  totalCount?: number;
}


export class ApiBatchResult implements IBatchResult {
  success?: boolean;
  message?: string;
  modifiedCount?: number;
  totalCount?: number;
  constructor({success, message, modifiedCount, totalCount}: IBatchResult) {
    this.success = success
    this.message = message
    this.modifiedCount = modifiedCount
    this.totalCount = totalCount
  }
}


