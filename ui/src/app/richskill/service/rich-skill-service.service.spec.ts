import {TestBed} from "@angular/core/testing"

import {RichSkillService} from "./rich-skill.service"

describe("RichSkillServiceService", () => {
  let service: RichSkillService

  beforeEach(() => {
    TestBed.configureTestingModule({})
    service = TestBed.inject(RichSkillService)
  })

  it("should be created", () => {
    expect(service).toBeTruthy()
  })
})
