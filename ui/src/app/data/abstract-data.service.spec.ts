import { TestBed } from "@angular/core/testing"

import { AbstractDataService } from "./abstract-data.service"

describe("AbstractAdminService", () => {
  let service: AbstractDataService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(AbstractDataService)
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
