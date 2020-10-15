import {TestBed} from "@angular/core/testing"
import {TaskService} from "./task-service"
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'
import {AuthService} from "../auth/auth-service";


describe("TaskService", () => {
  let service: TaskService
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    })
    service = TestBed.inject(TaskService)
    httpMock = TestBed.inject(HttpTestingController)
  })

  afterEach(() => {
    httpMock.verify()
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
