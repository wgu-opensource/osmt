import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing"
import {HeaderComponent} from "./header.component"
import {AuthService} from "../auth/auth-service"
import {AuthServiceStub} from "../../../test/resource/mock-stubs"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "../app.config"
import {EnvironmentService} from "../core/environment.service"
import {Location} from "@angular/common"
import {ConcreteService} from "../abstract.service.spec"
import {HttpClientModule} from "@angular/common/http"
import {Router} from "@angular/router"
import {MyWorkspaceComponent} from "../my-workspace/my-workspace.component"
import {RichSkillsLibraryComponent} from "../richskill/library/rich-skills-library.component"

describe("HeaderComponent", () => {

  let component: HeaderComponent
  let fixture: ComponentFixture<HeaderComponent>
  let router: Router

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        HeaderComponent
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        ConcreteService,
        Location,
        {provide: AuthService, useClass: AuthServiceStub},
      ],
      imports: [
        HttpClientModule,
        RouterTestingModule.withRoutes([
          {
            path: "my-workspace",
            component: MyWorkspaceComponent
          },
          {
            path: "skills",
            component: RichSkillsLibraryComponent
          }
        ])
      ]
    }).compileComponents()
  })

  beforeEach(() => {
    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
    fixture = TestBed.createComponent(HeaderComponent)
    component = fixture.componentInstance
    router = TestBed.inject(Router)
    fixture.detectChanges()
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("my workspace is active", fakeAsync(() => {
    router.navigate(["/my-workspace"])
    tick()
    expect(component.myWorkspaceActive).toBeTruthy()
  }))

  it("my workspace is active", fakeAsync(() => {
    router.navigate(["/skills"])
    tick()
    expect(router).toBeTruthy()
  }))


})
