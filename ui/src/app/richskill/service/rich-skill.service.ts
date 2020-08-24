import {Injectable} from "@angular/core"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import {Observable, of} from "rxjs"
import {RichSkill} from "../RichSkill"
import {catchError, tap} from "rxjs/operators"

@Injectable({
  providedIn: "root"
})
export class RichSkillService {

  constructor(private httpClient: HttpClient) {
  }

  private serviceUrl = "api/skills"

  getSkills(): Observable<RichSkill[]> {
    return this.httpClient.get<RichSkill[]>(this.serviceUrl)
  }
}
