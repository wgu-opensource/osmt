import {Injectable} from "@angular/core"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, ISkill} from "../ApiSkill"
import {map, share} from "rxjs/operators"
import {AbstractService} from "../../abstract.service"
import {ApiSkillUpdate} from "../ApiSkillUpdate"
import RuntimeError = WebAssembly.RuntimeError


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
      .pipe(share())
      .pipe(map(({body}) => {
        return body?.map(skill => new ApiSkill(skill)) || []
      }))
  }

  getSkillByUUID(uuid: string): Observable<ApiSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.get<ISkill>({
      path: `${this.serviceUrl}/${uuid}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }

  // These two nearly identical getSkill functions can probably be joined.  the angular httpclient is weird though
  // and overloads it's functions many times which makes any kind of abstraction seeking to broaden flexibility incompatible
  getSkillCsvByUuid(uuid: string): Observable<string> {
    if (!uuid) {
      throw new Error("No uuid provided for single skill csv export")
    }
    const errorMsg = `Could not find skill by uuid [${uuid}]`

    return this.httpClient
      .get(`${this.serviceUrl}/${uuid}`, {
        headers: {
          Accept: "text/csv"
        },
        responseType: "text",
        observe: "response"
      })
      .pipe(map((response) => this.safeUnwrapBody(response.body, errorMsg)))
  }

  getSkillJsonByUuid(uuid: string): Observable<string> {
    if (!uuid) {
      throw new Error("No uuid provided for single skill csv export")
    }
    const errorMsg = `Could not find skill by uuid [${uuid}]`

    return this.httpClient
      .get(`${this.serviceUrl}/${uuid}`, {
        headers: {
          Accept: "application/json"
        },
        responseType: "text",
        observe: "response"
      })
      .pipe(map((response) => this.safeUnwrapBody(response.body, errorMsg)))
  }

  createSkill(updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const errorMsg = `Error creating skill`
    return this.post<ISkill[]>({
      path: this.serviceUrl,
      body: [updateObject]
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiSkill(s))[0]))
  }

  updateSkill(uuid: string, updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.post<ISkill>({
      path: `${this.serviceUrl}/${uuid}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiSkill(this.safeUnwrapBody(body, errorMsg))))
  }
}
