import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { FormsModule } from "@angular/forms"
import { By } from "@angular/platform-browser"
import { Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { first } from "rxjs/operators"
import { AppConfig } from "src/app/app.config"
import { AuthService } from "src/app/auth/auth-service"
import { EnvironmentService } from "src/app/core/environment.service"
import { RichSkillService } from "src/app/richskill/service/rich-skill.service"
import { ToastService } from "src/app/toast/toast.service"
import { AuthServiceStub, RichSkillServiceStub } from "test/resource/mock-stubs"
import { ManageSkillActionBarVerticalComponent } from "./manage-skill-action-bar-vertical.component"
import any = jasmine.any


@Component({
  template: `
    <app-manage-skill-action-bar-vertical
      [skillUuid]="mySkillUuid"
      [skillName]="mySkillName"
      [skillPublicUrl]="mySkillPublicUrl"
      [archived]="myArchived"
      [published]="myPublished">
    </app-manage-skill-action-bar-vertical>`
})
class TestHostComponent {
  mySkillUuid = "1234"
  mySkillName = "my skill name"
  mySkillPublicUrl = "mockUrl"
  myArchived = false
  myPublished = false
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(ManageSkillActionBarVerticalComponent))
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
let childComponent: ManageSkillActionBarVerticalComponent


describe("ManageSkillActionBarVerticalComponent", () => {
  let toastService: ToastService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ManageSkillActionBarVerticalComponent,
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

    toastService = TestBed.inject(ToastService)

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
  })

  it("onAddToCollection should return", () => {
    // Arrange
    const router = TestBed.inject(Router)
    spyOn(router, "navigate").and.stub()

    // Act
    childComponent.onAddToCollection()

    // Assert
    expect(router.navigate).toHaveBeenCalledWith([ "/collections/add-skills" ], any(Object) )
  })

  it("publishLinkText should return", () => {
    // Arrange
    const expected = "Publish"

    // Act
    const result = childComponent.publishLinkText()

    // Assert
    expect(result).toEqual(expected)
  })

  it("publishLinkDestination should return", () => {
    // Arrange
    const expected = ""

    // Act
    const result = childComponent.publishLinkDestination()

    // Assert
    expect(result).toEqual(expected)
  })

  it("handleArchive should return", () => {
    // Arrange
    let clicked = false
    childComponent.reloadSkill.pipe(first()).subscribe(
      () => { clicked = true; return }
    )

    // Act
    childComponent.handleArchive()

    // Assert
    expect(clicked).toBeTruthy()
  })

  it("handleUnarchive should return", () => {
    // Arrange
    let clicked = false
    childComponent.reloadSkill.pipe(first()).subscribe(
      () => { clicked = true; return }
    )

    // Act
    childComponent.handleUnarchive()

    // Assert
    expect(clicked).toBeTruthy()
  })

  it("handlePublish should return", () => {
    // Arrange
    let clicked = false
    childComponent.reloadSkill.pipe(first()).subscribe(
      () => { clicked = true; return }
    )

    spyOn(window, "confirm").and.returnValue(true)

    // Act
    childComponent.handlePublish()

    // Assert
    expect(clicked).toBeTruthy()
  })

  it("handleCopyPublicUrl should return", async (done) => {
    // Arrange
    let clipboardWriteTextSpy = spyOn(navigator.clipboard, "writeText").and.returnValue(Promise.resolve())
    let showToastSpy = spyOn(toastService, "showToast").and.callFake(() => { done() })

    // Act
    childComponent.handleCopyPublicURL()

    await clipboardWriteTextSpy

    // Assert
    expect(clipboardWriteTextSpy).toHaveBeenCalledWith("mockUrl")
    expect(showToastSpy).toHaveBeenCalledWith("Success!", "URL copied to clipboard")
  })
})
