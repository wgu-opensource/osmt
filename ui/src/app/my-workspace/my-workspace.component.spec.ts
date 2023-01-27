import { ComponentFixture, TestBed } from "@angular/core/testing"

import { MyWorkspaceComponent } from "./my-workspace.component"
import {RouterTestingModule} from "@angular/router/testing"
import {HttpClientModule} from "@angular/common/http"
import {AuthService} from "../auth/auth-service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {AuthServiceStub, CollectionServiceStub, EnvironmentServiceStub, RichSkillServiceStub} from "../../../test/resource/mock-stubs"
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {AppConfig} from "../app.config"
import {Title} from "@angular/platform-browser"
import {ToastService} from "../toast/toast.service"
import {EnvironmentService} from "../core/environment.service"
import {CollectionService} from "../collection/service/collection.service"

describe("MyWorkspaceComponent", () => {
  let component: MyWorkspaceComponent
  let fixture: ComponentFixture<MyWorkspaceComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
      ],
      declarations: [ MyWorkspaceComponent ],
      providers: [
        AppConfig,
        Title,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },  // Example of using a service stub
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    }).compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(MyWorkspaceComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
