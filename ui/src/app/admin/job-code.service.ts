import { Injectable } from "@angular/core"
import {AbstractAdminService} from "./abstract-admin.service"
import {HttpClient} from "@angular/common/http"

@Injectable({
  providedIn: "root"
})
export class JobCodeService extends AbstractAdminService{

  private baseServiceUrl = "api/job-codes"


  constructor(protected httpClient: HttpClient) {
    super(httpClient)
  }
}
