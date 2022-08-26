import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { FormsModule } from "@angular/forms"
import { By } from "@angular/platform-browser"
import { RouterTestingModule } from "@angular/router/testing"
import { AppConfig } from "src/app/app.config"
import { AuthService } from "src/app/auth/auth-service"
import { EnvironmentService } from "src/app/core/environment.service"
import { RichSkillService } from "src/app/richskill/service/rich-skill.service"
import { ToastService } from "src/app/toast/toast.service"
import { AuthServiceStub, RichSkillServiceStub } from "test/resource/mock-stubs"
import { ManageSkillActionBarHorizontalComponent } from "./manage-skill-action-bar-horizontal.component"


@Component({
  template: `
    <app-manage-skill-action-bar-horizontal
      [skillUuid]="mySkillUuid"
      [skillName]="mySkillName"
      [archived]="myArchived"
      [published]="myPublished">
    </app-manage-skill-action-bar-horizontal>`
})
class TestHostComponent {
  mySkillUuid = "1234"
  mySkillName = "my skill name"
  myArchived = false
  myPublished = false
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(ManageSkillActionBarHorizontalComponent))
  childComponent = debugEl.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  hostFixture.detectChanges()

  return hostFixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    hostFixture.detectChanges()
  })
}


let hostFixture: ComponentFixture<TestHostComponent>
let hostComponent: TestHostComponent
let childComponent: ManageSkillActionBarHorizontalComponent


describe("ManageSkillActionBarHorizontalComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ManageSkillActionBarHorizontalComponent,
        TestHostComponent
      ],
      imports: [
        FormsModule,  // Required for ([ngModel])
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        ToastService,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
  })
})
