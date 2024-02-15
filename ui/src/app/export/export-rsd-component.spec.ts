import { HttpClientTestingModule } from "@angular/common/http/testing"
import { ComponentFixture, TestBed } from "@angular/core/testing"

import { ExportRsdComponent } from "./export-rsd.component"
import { RichSkillService } from "../richskill/service/rich-skill.service"
import { RichSkillServiceStub } from "../../../test/resource/mock-stubs"
import {PublishStatus} from "../PublishStatus"
import {ApiSearch} from "../richskill/service/rich-skill-search.service"

describe("ExportRsdComponent", () => {
  let component: ExportRsdComponent
  let fixture: ComponentFixture<ExportRsdComponent>
  let richSkillService: RichSkillService
  const statuses = new Set<PublishStatus>([PublishStatus.Published, PublishStatus.Draft])

  beforeEach(async () => {
    TestBed.configureTestingModule({
      declarations: [
        ExportRsdComponent
      ],
      providers: [
        { provide: RichSkillService, useClass: RichSkillServiceStub },
      ],
      imports: [
        HttpClientTestingModule
      ]
    })
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(ExportRsdComponent)
    richSkillService = TestBed.inject(RichSkillService)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("getRsdCsv should work", () => {
    const uuid = "384c3133-2e1b-4703-9225-b666ccda5879"
    const entityName = "RSD name"
    const spyLoader = spyOn(component["toastService"], "showBlockingLoader")
    const spyService = spyOn(richSkillService, "getSkillCsvByUuid").and.callThrough()
    component.getRsdCsv(uuid, entityName)
    expect(spyLoader)
    expect(spyService).toHaveBeenCalled()
  })

  it("getRsdXlsx should work", () => {
    const uuid = "384c3133-2e1b-4703-9225-b666ccda5879"
    const entityName = "RSD name"
    const spyLoader = spyOn(component["toastService"], "showBlockingLoader")
    const spyService = spyOn(richSkillService, "exportSearchXlsx").and.callThrough()
    component.getRsdXlsx(uuid, entityName)
    expect(spyLoader)
    expect(spyService).toHaveBeenCalled()
  })

  it("exportSearchCsv should return", () => {
    const uuids = ["384c3133-2e1b-4703-9225-b666ccda5879"]
    const entityName = ["RSD name"]
    const spyLoader = spyOn(component["toastService"], "showBlockingLoader")
    const spyService = spyOn(richSkillService, "exportSearchCsv").and.callThrough()
    component.exportSearchCsv(new ApiSearch({uuids}), entityName, statuses)
    expect(spyLoader)
    expect(spyService).toHaveBeenCalled()
  })

  it("exportSearchXlsx should return", () => {
    const uuids = ["384c3133-2e1b-4703-9225-b666ccda5879"]
    const entityName = ["RSD name"]
    const spyLoader = spyOn(component["toastService"], "showBlockingLoader")
    const spyService = spyOn(richSkillService, "exportSearchXlsx").and.callThrough()
    component.exportSearchXlsx(new ApiSearch({uuids}), entityName, statuses)
    expect(spyLoader)
    expect(spyService).toHaveBeenCalled()
  })
})
