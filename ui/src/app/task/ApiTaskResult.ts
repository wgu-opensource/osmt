export interface ITaskResult {
  status: string
  contentType: string
  id: string
  uuid: string | undefined
}

export class ApiTaskResult implements ITaskResult{
  contentType: string
  id: string
  status: string
  uuid: string

  constructor(task: ITaskResult) {
    this.contentType = task.contentType
    this.id = task.id
    this.status = task.status
    this.uuid = task.uuid || ""
  }
}
