import {TestBed} from "@angular/core/testing"

import { RichSkillService } from "./rich-skill.service"
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'

describe("RichSkillServiceService", () => {
  let service: RichSkillService
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    })
    service = TestBed.inject(RichSkillService)
    httpMock = TestBed.inject(HttpTestingController)
  })

  afterEach(() => {
    httpMock.verify()
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
