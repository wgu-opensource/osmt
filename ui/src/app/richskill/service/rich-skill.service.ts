import {Injectable} from "@angular/core"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ISkill, ApiSkill} from "../ApiSkill"
import {map} from "rxjs/operators"
import {AbstractService} from "../../abstract.service"
import {IRichSkillUpdate, ApiSkillUpdate} from "../ApiSkillUpdate";


@Injectable({
  providedIn: "root"
})
export class RichSkillService extends AbstractService {

  constructor(httpClient: HttpClient) {
    super(httpClient)
  }

  private serviceUrl = "api/skills"

  getSkills(): Observable<ApiSkill[]> {
    return this.get<ISkill[]>({
      path: this.serviceUrl
    })
      .pipe(map(({body}) => {
        return body?.map(skill => new ApiSkill(skill)) || []
      }))
  }

  getSkillByUUID(uuid: string): Observable<ApiSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.get<ISkill>({
      path: `${this.serviceUrl}/${uuid}`
    })
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }

  createSkill(updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const errorMsg = `Error creating skill`
    return this.post<ISkill>({
      path: this.serviceUrl,
      body: [updateObject]
    })
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }

  updateSkill(uuid: string, updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.post<ISkill>({
      path: `${this.serviceUrl}/${uuid}/update`,
      body: updateObject
    })
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }
}
