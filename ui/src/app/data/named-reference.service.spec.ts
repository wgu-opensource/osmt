import { TestBed } from "@angular/core/testing"

import { NamedReferenceService } from "./named-reference.service"

describe("NamedReferenceService", () => {
  let service: NamedReferenceService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(NamedReferenceService)
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
