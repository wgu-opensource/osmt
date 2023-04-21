import { TestBed } from "@angular/core/testing"

import { AbstractAdminService } from "./abstract-admin.service"

describe("AbstractAdminService", () => {
  let service: AbstractAdminService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(AbstractAdminService)
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
