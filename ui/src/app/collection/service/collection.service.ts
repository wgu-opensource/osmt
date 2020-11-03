import {HttpClient} from "@angular/common/http"
import {AbstractService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {Injectable} from "@angular/core"
import {Observable} from "rxjs"
import {Collection, CollectionUpdate} from "../Collection"
import {map, share} from "rxjs/operators"

@Injectable({
  providedIn: "root"
})
export class CollectionService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
  }

  create(collections: CollectionUpdate[]): Observable<Collection> {
    return this.post<Collection>({
      path: "/api/collections",
      body: collections
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, "")))
  }
}
