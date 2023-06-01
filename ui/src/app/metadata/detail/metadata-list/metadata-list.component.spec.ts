import { ComponentFixture, TestBed } from "@angular/core/testing"
import { MetadataListComponent } from "./metadata-list.component"
import { MetadataType } from "../../rsd-metadata.enum"
import { PaginatedMetadata } from "../../PaginatedMetadata"
import { ApiJobCode } from "../../job-codes/Jobcode"
import { AuthServiceStub } from "@test/resource/mock-stubs";
import { AuthService } from "../../../auth/auth-service";
import { JobCodeService } from "../../job-codes/service/job-code.service"
import { HttpClientTestingModule } from "@angular/common/http/testing"

describe("ManageMetadataComponent", () => {
  let component: MetadataListComponent
  let fixture: ComponentFixture<MetadataListComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataListComponent ],
      providers: [
        { provide: AuthService, useClass: AuthServiceStub },
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
})
