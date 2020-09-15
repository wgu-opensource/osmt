import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Observable } from "rxjs"
import { ITaskResponse, TaskInProgress } from "./TaskInProgress"
import { map } from "rxjs/operators"

@Injectable({
  providedIn: "root"
})
export class TaskService {
  constructor(private httpClient: HttpClient) {
  }

  private serviceUrl = "api/tasks/"


  startAllSkillsCsvTask(): Observable<ITaskResponse> {
    return this.httpClient
      .get<ITaskResponse>(`api/skills`, {
        headers: {
          Accept: "text/csv"
        }
      })
      .pipe(map((xs: ITaskResponse) => new TaskInProgress(xs)))
  }

  // tslint:disable-next-line:no-any
  getTaskResultsIfComplete(uuid: string): Observable<any> {
    return this.httpClient
      .get(this.serviceUrl + "/" + uuid, {
        responseType: "text",
        observe: "response"
      })
  }
}
