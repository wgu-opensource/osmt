export interface ITaskResponse {
  status: string
  contentType: string
  id: string
  uuid: string | undefined
}

export class TaskInProgress implements ITaskResponse{
  contentType: string
  id: string
  status: string
  uuid: string

  constructor(task: ITaskResponse) {
    this.contentType = task.contentType
    this.id = task.id
    this.status = task.status
    this.uuid = task.uuid || ""
  }
}
