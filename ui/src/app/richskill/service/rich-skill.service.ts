import {Injectable} from "@angular/core"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import {Observable, of} from "rxjs"
import {IRichSkillResponse, RichSkill} from "../RichSkill"
import {catchError, map, tap} from "rxjs/operators"

@Injectable({
  providedIn: "root"
})
export class RichSkillService {

  constructor(private httpClient: HttpClient) {
  }

  private serviceUrl = "api/skills"

  getSkills(): Observable<RichSkill[]> {
    return this.httpClient.get<IRichSkillResponse[]>(this.serviceUrl).pipe(map((xs: IRichSkillResponse[]) => xs.map(x => new RichSkill(x))))
  }

  getSkillByUUID(uuid: string): Observable<RichSkill> {
    return this.httpClient.get<IRichSkillResponse>(`${this.serviceUrl}/${uuid}`).pipe(map((irs: IRichSkillResponse) => new RichSkill(irs)))
  }
}
