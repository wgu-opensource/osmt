import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { createMockSkill } from "../../../../test/resource/mock-data"
import { RichSkillServiceStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { dateformat } from "../../core/DateHelper"
import { EnvironmentService } from "../../core/environment.service"
import { IDetailCardSectionData } from "../../detail-card/section/section.component"
import { PublishStatus } from "../../PublishStatus"
import { ApiSkill } from "../ApiSkill"
import { RichSkillService } from "../service/rich-skill.service"
import { AbstractRichSkillDetailComponent } from "./AbstractRichSkillDetailComponent"


@Component({
  selector: "app-concrete-component",
  template: ``
})
class ConcreteComponent extends AbstractRichSkillDetailComponent {
  getCardFormat(): IDetailCardSectionData[] {
    return []
  }

  get _locale(): string {
    return this.locale
  }

  public formatAssociatedCollections(isAuthorized: boolean): string {
    return super.formatAssociatedCollections(isAuthorized)
  }
}


export function createComponent(T: Type<ConcreteComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let activatedRoute: ActivatedRouteStubSpec
let component: ConcreteComponent
let fixture: ComponentFixture<ConcreteComponent>


describe("ConcreteComponent", () => {
  const date = new Date("2020-06-25T14:58:46.313Z")
  const skill = new ApiSkill(createMockSkill(date, date, PublishStatus.Draft))

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        ConcreteComponent
      ],
      imports: [
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    activatedRoute.setParamMap({ uuid: "126" })
    createComponent(ConcreteComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()

    expect(component.uuidParam).toEqual("126")
  })

  it("getAuthor should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getAuthor()).toEqual("")

    // Arrange
    component.richSkill = skill
    // Act/Assert
    expect(component.getAuthor()).toEqual(skill.author.name as string)
  })

  it("getUuid should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getSkillUuid()).toEqual("")

    // Arrange
    component.richSkill = skill
    // Act/Assert
    expect(component.getSkillUuid()).toEqual(skill.uuid)
  })

  it("getSkillName should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getSkillName()).toEqual("")

    // Arrange
    component.richSkill = skill
    // Act/Assert
    expect(component.getSkillName()).toEqual(skill.skillName)
  })

  it("getPublishStatus should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getPublishStatus()).toEqual(PublishStatus.Draft)

    // Arrange
    component.richSkill = skill
    // Act/Assert
    expect(component.getPublishStatus()).toEqual(skill.status)
  })

  it("getSkillUrl should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getSkillUrl()).toEqual("")

    // Arrange
    component.richSkill = skill
    // Act/Assert
    expect(component.getSkillUrl()).toEqual(skill.id)
  })

  it("getPublishedDate should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getPublishedDate()).toEqual("")

    // Arrange
    skill.publishDate = date
    component.richSkill = skill
    // Act/Assert
    expect(component.getPublishedDate()).toEqual(dateformat(skill.publishDate, component._locale))
  })

  it("getArchivedDate should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.getArchivedDate()).toEqual("")

    // Arrange
    skill.archiveDate = date
    component.richSkill = skill
    // Act/Assert
    expect(component.getArchivedDate()).toEqual(dateformat(skill.archiveDate, component._locale))
  })

  it("joinKeywords should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.joinKeywords()).toEqual("")

    // Arrange
    skill.archiveDate = date
    component.richSkill = skill
    // Act/Assert
    expect(component.joinKeywords()).toEqual(skill.keywords.join("; "))
  })

  it("joinEmployers should return", () => {
    // Arrange
    component.richSkill = null
    // Act/Assert
    expect(component.joinEmployers()).toEqual("")

    // Arrange
    const expected = "Acme; Joe's Pizza"
    skill.employers = [ { name: "Acme" }, { name: "Joe's Pizza"}]
    component.richSkill = skill
    // Act/Assert
    expect(component.joinEmployers()).toEqual(expected)
  })

  /* tslint:disable:quotemark */
  it("formatAssociatedCollections should return", () => {
    // Arrange
    const expected =
      '<div><a class="t-link" href="/collections/1/manage">Intro</a></div><br>' +
      '<div><a class="t-link" href="/collections/2/manage">Advanced</a></div>'
    skill.collections = [ { uuid: "1", name: "Intro" }, { uuid: "2", name: "Advanced" } ]
    component.richSkill = skill
    // Act/Assert
    expect(component.formatAssociatedCollections(true)).toEqual(expected)
  })
})
