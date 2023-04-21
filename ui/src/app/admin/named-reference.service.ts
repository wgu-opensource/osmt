import { Injectable } from "@angular/core"
import {AbstractAdminService} from "./abstract-admin.service"
import {HttpClient} from "@angular/common/http"

@Injectable({
  providedIn: "root"
})
export class NamedReferenceService extends AbstractAdminService{

  private baseServiceUrl = "api/named-references"

  constructor(protected httpClient: HttpClient) {
    super(httpClient)
  }
}
