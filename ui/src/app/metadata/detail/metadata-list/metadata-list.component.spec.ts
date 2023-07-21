import { ComponentFixture, TestBed } from "@angular/core/testing"
import { MetadataListComponent } from "./metadata-list.component"
import { MetadataType } from "../../rsd-metadata.enum"
import { PaginatedMetadata } from "../../PaginatedMetadata"
import { ApiJobCode } from "../../job-code/Jobcode"
import { AuthServiceStub } from "@test/resource/mock-stubs";
import { AuthService } from "../../../auth/auth-service";
import { JobCodeService } from "../../job-code/service/job-code.service"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import {createMockJobcode, createMockNamedReference2 } from "@test/resource/mock-data"
import { of } from "rxjs"
import { getBaseApi } from "../../../api-versions"

describe("ManageMetadataComponent", () => {
  let component: MetadataListComponent
  let fixture: ComponentFixture<MetadataListComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataListComponent ],
      providers: [
        { provide: AuthService, useClass: AuthServiceStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
        JobCodeService
      ],
      imports: [
        HttpClientTestingModule
      ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataListComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("isJobCodeDataSelected returns false if JobCode MetadataType is not selected", () => {
    expect(component.isJobCodeDataSelected).toBeFalse()
  })

  it("isJobCodeDataSelected returns true if JobCode MetadataType is selected", () => {
    component.selectedMetadataType = MetadataType.JobCode
    expect(component.isJobCodeDataSelected).toBeTrue()
  })

  it("emptyResults returns true if Metadata is empty", () => {
    component.results = new PaginatedMetadata([], 0)
    expect(component.emptyResults).toBeTrue()
  })

  it("emptyResults returns false if Metadata is not empty", () => {
    component.results = new PaginatedMetadata([new ApiJobCode(), new ApiJobCode()], 2)
    expect(component.emptyResults).toBeFalse()
  })

  it("matching query should be updated", () => {
    const matchingQuery = "95-0000"
    component.searchForm.get("search")?.patchValue(matchingQuery)
    component.handleDefaultSubmit()
    expect(component.matchingQuery).toEqual(matchingQuery)
  })

  it("When clearSearch is called matching query should be empty", () => {
    component.matchingQuery = "This is a query"
    component.clearSearch()
    expect(component.matchingQuery).toBe("")
  })

  it("handleDeleteJobCode should call deleteJobCodeWithResult", () => {
    const mockJobCode = createMockJobcode()
    const spyService = spyOn(component["jobCodeService"], "deleteJobCodeWithResult").and.returnValue(
      of({success: true})
    )
    spyOn(window, 'confirm').and.callFake(function () {
      return true;
    });
    component["handleDeleteJobCode"](mockJobCode)
    expect(spyService).toHaveBeenCalled()
  })

  it("handleDeleteJobCode should not call deleteJobCodeWithResult", () => {
    const mockJobCode = createMockJobcode()
    const spyService = spyOn(component["jobCodeService"], "deleteJobCodeWithResult").and.returnValue(
      of({success: true})
    )
    spyOn(window, 'confirm').and.callFake(function () {
      return false;
    });
    component["handleDeleteJobCode"](mockJobCode)
    expect(spyService).not.toHaveBeenCalled()
  })

  it("handleDeleteNamedReference should call deleteNamedReferenceWithResult", () => {
    const mockNamedReference = createMockNamedReference2()
    const spyService = spyOn(component["namedReferenceService"], "deleteNamedReferenceWithResult").and.returnValue(
      of({success: true})
    )
    spyOn(window, 'confirm').and.callFake(function () {
      return true;
    });
    component["handleDeleteNamedReference"](mockNamedReference)
    expect(spyService).toHaveBeenCalled()
  })

  it("handleDeleteNamedReference should not call deleteNamedReferenceWithResult", () => {
    const mockNamedReference = createMockNamedReference2()
    const spyService = spyOn(component["namedReferenceService"], "deleteNamedReferenceWithResult").and.returnValue(
      of({success: true})
    )
    spyOn(window, 'confirm').and.callFake(function () {
      return false;
    });
    component["handleDeleteNamedReference"](mockNamedReference)
    expect(spyService).not.toHaveBeenCalled()
  })
})
