import {TestBed} from "@angular/core/testing"
import {TaskService} from "./task-service"
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'


describe("TaskService", () => {
  let service: TaskService
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
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
