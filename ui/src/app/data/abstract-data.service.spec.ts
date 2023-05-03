import { TestBed } from "@angular/core/testing"
import { Router } from "@angular/router"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Location } from "@angular/common"
import { AbstractDataService } from "./abstract-data.service"
import { EnvironmentService } from "../core/environment.service"
import { AppConfig } from "../app.config"
import { AuthService } from "../auth/auth-service"
import { AuthServiceStub, RouterStub } from "@test/resource/mock-stubs"

describe("AbstractAdminService", () => {
  let testService: AbstractDataService

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        EnvironmentService,
        AppConfig,
        AbstractDataService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ]})
    testService = TestBed.inject(AbstractDataService)
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })
})
