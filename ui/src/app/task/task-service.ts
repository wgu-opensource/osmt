import {Injectable} from "@angular/core"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {share} from "rxjs/operators"
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


  // this call is a bit different since it's returning a csv for immediate download, so use httpClient's get() method
  // tslint:disable-next-line:no-any
  getTaskResultsIfComplete(uuid: string): Observable<any> {
    return this.httpClient
      .get(`api/results/text/${uuid}`, {
        responseType: "text",
        observe: "response"
      })
      .pipe(share())
  }
}
