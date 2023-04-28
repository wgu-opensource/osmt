import { TestBed } from "@angular/core/testing"

import { NamedReferenceService } from "./named-reference.service"
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {EnvironmentService} from "../core/environment.service"
import {AppConfig} from "../app.config"
import {JobCodeService} from "../job-codes/service/job-code.service"
import {Location} from "@angular/common"
import {AuthService} from "../auth/auth-service"
import {AuthServiceStub, RouterStub} from "../../../test/resource/mock-stubs"
import {Router} from "@angular/router"

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
        { provide: Router, useClass: RouterStub }
      ]})
    testService = TestBed.inject(NamedReferenceService)
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })
})
