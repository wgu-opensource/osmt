import { Injectable } from "@angular/core"
import {HttpClient} from "@angular/common/http"

@Injectable({
  providedIn: "root"
})
export abstract class AbstractAdminService {

  protected constructor(httpClient: HttpClient) { }
}
