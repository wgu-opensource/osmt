/* tslint:disable:no-string-literal */
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ActivatedRoute, Router } from "@angular/router"
import * as FileSaver from "file-saver"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { AuthService } from "../auth/auth-service"
import { RichSkillService } from "../richskill/service/rich-skill.service"
import { AuthServiceStub, RichSkillServiceStub } from "../../../test/resource/mock-stubs"
import { LibraryExportComponent } from "./libraryexport.component"
import { of } from "rxjs"
import { apiTaskResultForCSV } from "../../../test/resource/mock-data"


let component: LibraryExportComponent
let fixture: ComponentFixture<LibraryExportComponent>
let activatedRoute: ActivatedRouteStubSpec

export function createComponent(T: Type<LibraryExportComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}

describe("LibraryExportComponent", () => {
  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()
    activatedRoute = new ActivatedRouteStubSpec()

    TestBed.configureTestingModule({
      declarations: [
        LibraryExportComponent
      ],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: Router, useValue: routerSpy }
      ]
    })
      .compileComponents()
    createComponent(LibraryExportComponent)
    spyOn(FileSaver, "saveAs").and.stub()
  }))

  it("should be created", () => {
    expect(LibraryExportComponent).toBeTruthy()
  })

  it("Should call export library with result", () => {
    const service = TestBed.inject(RichSkillService)
    const spy = spyOn(service, "exportLibraryWithResult").and.returnValue(of(apiTaskResultForCSV))
    component.onDownloadLibrary()
    expect(spy).toHaveBeenCalled()
    // expect(FileSaver.saveAs).toHaveBeenCalled()
  })


  it("download as csv file", () => {
    component["downloadAsCsvFile"]("value1,value2,value3")
    expect(FileSaver.saveAs).toHaveBeenCalled()
  })
})
