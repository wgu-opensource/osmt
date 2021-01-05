import { Injectable } from "@angular/core"
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http"
import { Observable } from "rxjs"
import { ITaskResult, ApiTaskResult } from "./ApiTaskResult"
import {map, share} from "rxjs/operators"
import {AbstractService} from "../abstract.service"
import {AuthService} from "../auth/auth-service";
import {Router} from "@angular/router";
import {Location} from "@angular/common";

@Injectable({
  providedIn: "root"
})
export class TaskService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

  private serviceUrl = "api/tasks/"


  startAllSkillsCsvTask(): Observable<ITaskResult> {
    const failureMessage = "Could not start a skills csv export"
    return this.get<ITaskResult>({
      path: "api/skills",
      headers: new HttpHeaders({
        Accept: "text/csv"
      })
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, failureMessage))))
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
