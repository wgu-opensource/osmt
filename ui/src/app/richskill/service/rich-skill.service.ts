import {Injectable} from "@angular/core"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {IRichSkillResponse, RichSkill} from "../RichSkill"
import {map} from "rxjs/operators"
import {AbstractService} from "../../abstract.service"


@Injectable({
  providedIn: "root"
})
export class RichSkillService extends AbstractService {

  constructor(httpClient: HttpClient) {
    super(httpClient)
  }

  private serviceUrl = "api/skills"

  getSkills(): Observable<RichSkill[]> {
    return this.get<IRichSkillResponse[]>({
      path: this.serviceUrl
    })
      .pipe(map(({body}) => {
        return body?.map(skill => new RichSkill(skill)) || []
      }))
  }

  getSkillByUUID(uuid: string): Observable<RichSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.get<IRichSkillResponse>({
      path: `${this.serviceUrl}/${uuid}`
    })
      .pipe(map(({body}) => new RichSkill(this.safeUnwrapBody(body, errorMsg))))
  }
}
