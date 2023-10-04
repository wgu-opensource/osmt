import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing"
import {HeaderComponent} from "./header.component"
import {AuthService} from "../auth/auth-service"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "../app.config"
import {EnvironmentService} from "../core/environment.service"
import {Location} from "@angular/common"
import {ConcreteService} from "../abstract.service.spec"
import {HttpClientModule} from "@angular/common/http"
import {Router} from "@angular/router"
import {MyWorkspaceComponent} from "../my-workspace/my-workspace.component"
import {RichSkillsLibraryComponent} from "../richskill/library/rich-skills-library.component"
import {ButtonAction} from "../auth/auth-roles"
import {By} from "@angular/platform-browser"
import { Idle, IdleExpiry } from "@ng-idle/core"
import { Keepalive } from "@ng-idle/keepalive"
import { MatMenuModule } from "@angular/material/menu";

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
        AuthService,
        Idle,
        IdleExpiry,
        Keepalive
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
        ]),
        MatMenuModule
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

  it("my workspace is not visible when user doesn't have role admin or curator", () => {
    const authService = TestBed.inject(AuthService)
    spyOn(authService, "isEnabledByRoles").and.returnValue( false)
    component.canHaveWorkspace = component["authService"].isEnabledByRoles(ButtonAction.MyWorkspace)
    fixture.detectChanges()
    const myWorkspace = fixture.debugElement.query(By.css("#li-my-workspace"))
    expect(myWorkspace).toBeFalsy()
    expect(component.canHaveWorkspace).toBeFalse()
  })

  it("canHaveWorkspace should be false", () => {
    const authService = TestBed.inject(AuthService)
    spyOn(authService, "getRole").and.returnValue("ROLE_Osmt_Viewer")
    component.canHaveWorkspace = component["authService"].isEnabledByRoles(ButtonAction.MyWorkspace)
    expect(component.canHaveWorkspace).toBeFalse()
  })

  it("canHaveWorkspace should be true", () => {
    const authService = TestBed.inject(AuthService)
    spyOn(authService, "getRole").and.returnValue("ROLE_Osmt_Admin")
    component.canHaveWorkspace = component["authService"].isEnabledByRoles(ButtonAction.MyWorkspace)
    expect(component.canHaveWorkspace).toBeTrue()
  })

  it("skills is active", fakeAsync(() => {
    router.navigate(["/skills"])
    tick()
    expect(router).toBeTruthy()
  }))


})
