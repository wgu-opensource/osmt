import { Injectable } from "@angular/core"
import {HttpClient, HttpResponse} from "@angular/common/http"
import { Observable } from "rxjs"
import { ITaskResponse, TaskInProgress } from "./TaskInProgress"
import {map, share} from "rxjs/operators"
import {AbstractService} from "../abstract.service"

@Injectable({
  providedIn: "root"
})
export class TaskService extends AbstractService {

  constructor(httpClient: HttpClient) {
    super(httpClient)
  }

  private serviceUrl = "api/tasks/"


  startAllSkillsCsvTask(): Observable<ITaskResponse> {
    const failureMessage = "Could not start a skills csv export"
    return this.get<ITaskResponse>({
      path: "api/skills",
      headers: {
        Accept: "text/csv"
      }
    })
      .pipe(share())
      .pipe(map(({body}) => new TaskInProgress(this.safeUnwrapBody(body, failureMessage))))
  }

  // this call is a bit different since it's returning a csv for immediate download, so use httpClient's get() method
  // tslint:disable-next-line:no-any
  getTaskResultsIfComplete(uuid: string): Observable<any> {
    return this.httpClient
      .get(this.serviceUrl + uuid, {
        responseType: "text",
        observe: "response"
      })
      .pipe(share())
  }
}
