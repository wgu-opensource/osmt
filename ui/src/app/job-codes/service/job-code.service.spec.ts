import { TestBed } from "@angular/core/testing"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import { JobCodeService } from "./job-code.service"
import { HttpClient} from "@angular/common/http"
import {AuthServiceStub, RouterStub} from "../../../../test/resource/mock-stubs"
import {AppConfig} from "../../app.config"
import {EnvironmentService} from "../../core/environment.service"
import {CollectionService} from "../../collection/service/collection.service"
import {Location} from "@angular/common"
import {AuthService} from "../../auth/auth-service"
import {Router} from "@angular/router"


describe("JobCodeService", () => {
  let testService: JobCodeService

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        EnvironmentService,
        AppConfig,
        JobCodeService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ]})
    testService = TestBed.inject(JobCodeService)
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })
})
