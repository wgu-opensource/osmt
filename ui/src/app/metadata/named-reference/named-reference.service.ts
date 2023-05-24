import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Router } from "@angular/router"
import { Location } from "@angular/common"
import { AuthService } from "../../auth/auth-service"
import { AbstractDataService } from "../abstract-data.service"
import { ApiNamedReference } from "./NamedReference"

@Injectable({
  providedIn: "root"
})
export class NamedReferenceService extends AbstractDataService{

  private baseServiceUrl = "api/named-references"

  constructor(protected httpClient: HttpClient, protected authService: AuthService,
              protected router: Router, protected location: Location) {
    super(httpClient, authService, router, location)
  }
}

export class PaginatedNamedReferences {
  totalCount = 0
  namedReferences: ApiNamedReference[] = []
  constructor(namedReferences: ApiNamedReference[], totalCount: number) {
    this.namedReferences = namedReferences
    this.totalCount = totalCount
  }
}
