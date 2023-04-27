import { TestBed } from "@angular/core/testing"

import { JobCodeService } from "./job-code.service"

describe("JobCodeService", () => {
  let service: JobCodeService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(JobCodeService)
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
