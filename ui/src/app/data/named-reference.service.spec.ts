import { TestBed } from "@angular/core/testing"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { NamedReferenceService } from "./named-reference.service"
import { Location } from "@angular/common"
import { Router } from "@angular/router"
import { EnvironmentService } from "../core/environment.service"
import { AppConfig } from "../app.config"
import { AuthService } from "../auth/auth-service"
import { AuthServiceStub, RouterStub } from "@test/resource/mock-stubs"
import { getBaseApi } from "../api-versions"

describe("NamedReferenceService", () => {
  let testService: NamedReferenceService

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        EnvironmentService,
        AppConfig,
        NamedReferenceService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
      ]})
    testService = TestBed.inject(NamedReferenceService)
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })
})
