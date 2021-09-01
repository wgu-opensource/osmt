import { HttpClient } from "@angular/common/http"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { CollectionServiceStub, RichSkillServiceStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { CollectionService } from "../../collection/service/collection.service"
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { ApiAuditLog, AuditOperationType } from "../ApiSkill"
import { RichSkillService } from "../service/rich-skill.service"
import { AuditLogComponent } from "./audit-log.component"


export function createComponent(T: Type<AuditLogComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: AuditLogComponent
let fixture: ComponentFixture<AuditLogComponent>


describe("AuditLogComponent", () => {
  let httpClient: HttpClient
  let httpTestingController: HttpTestingController

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AuditLogComponent
      ],
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    httpClient = TestBed.inject(HttpClient)
    httpTestingController = TestBed.inject(HttpTestingController)

    createComponent(AuditLogComponent)
  }))

  afterEach(() => {
    httpTestingController.verify()
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("toggle should return", () => {
    // Arrange
    spyOn(component, "fetch")

    // Act
    component.toggle()

    // Assert
    expect(component.fetch).toHaveBeenCalled()
  })

  it("fetchLog should return nothing", () => {
    // Arrange
    const expected = undefined
    component.skillUuid = undefined
    component.collectionUuid = undefined

    // Act
    const result = component.fetchLog()

    // Assert
    expect(result).toEqual(expected)
  })

  it("iconForEntry should return", () => {
    [
      { op: AuditOperationType.Insert, new: "", expected: component.editIcon },
      { op: AuditOperationType.Update, new: "", expected: component.editIcon },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Published, expected: component.publishIcon },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Archived, expected: component.archiveIcon },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Unarchived, expected: component.unarchiveIcon },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Deleted, expected: component.archiveIcon },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Draft, expected: component.unarchiveIcon },
      { op: AuditOperationType.PublishStatusChange, new: "", expected: component.publishIcon },
    ].forEach((params) => {
      const entry = new ApiAuditLog({
        creationDate: "",
        operationType: params.op,
        user: "",
        changedFields: [ { fieldName: "foo", new: params.new, old: "" } ] })
      expect(component.iconForEntry(entry)).toEqual(params.expected)
    })
  })

  it("labelForEntry should return", () => {
    [
      { op: AuditOperationType.Insert, new: "", old: "-", expected: "Created" },
      { op: AuditOperationType.Update, new: "", old: "-", expected: "Edited" },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Deleted, old: "-", expected: "Archived" },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Draft, old: "-", expected: "Unarchived" },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Published, old: "Archived", expected: "Unarchived" },
      { op: AuditOperationType.PublishStatusChange, new: PublishStatus.Published, old: "Draft", expected: "Published" },
      { op: AuditOperationType.PublishStatusChange, new: "foo", old: "", expected: "foo"}
    ].forEach((params) => {
      const entry = new ApiAuditLog({
        creationDate: "",
        operationType: params.op,
        user: "",
        changedFields: [ { fieldName: "foo", new: params.new, old: params.old } ] })
      expect(component.labelForEntry(entry)).toEqual(params.expected)
    })
  })

  it("visibleFieldName should return", () => {
    [
      { fieldname: "sTATEMENT", expected: "Skill Statement" },
      { fieldname: "statement", expected: "Skill Statement" },
      { fieldname: "publishstatus", expected: "Publish Status" },
      { fieldname: "searchingkeywords", expected: "Keywords" },
      { fieldname: "alignments", expected: "Alignment" },
      { fieldname: "jobcodes", expected: "Occupations" },
      { fieldname: "foo", expected: "foo" },
    ].forEach((params) => {
      expect(component.visibleFieldName(params.fieldname)).toEqual(params.expected)
    })
  })
})
