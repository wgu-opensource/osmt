import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { RouterTestingModule } from "@angular/router/testing"
import { AppConfig } from "src/app/app.config"
import { EnvironmentService } from "src/app/core/environment.service"
import { createMockSkill } from "../../../../../test/resource/mock-data"
import { AuthServiceStub } from "../../../../../test/resource/mock-stubs"
import { AuthService } from "../../../auth/auth-service"
import { IDetailCardSectionData } from "../../../detail-card/section/section.component"
import { PublishStatus } from "../../../PublishStatus"
import { ApiSkill } from "../../ApiSkill"
import { RichSkillService } from "../../service/rich-skill.service"
import { RichSkillManageComponent } from "./rich-skill-manage.component"
import { getBaseApi } from "../../../api-versions"


export function createComponent(T: Type<RichSkillManageComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: RichSkillManageComponent
let fixture: ComponentFixture<RichSkillManageComponent>


describe("RichSkillManageComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        RichSkillManageComponent
      ],
      imports: [
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        RichSkillService,
        { provide: AuthService, useClass: AuthServiceStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    createComponent(RichSkillManageComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("getCardFormat should return", () => {
    // Arrange
    const now = new Date()
    const skill = createMockSkill(now, now, PublishStatus.Published)
    const expected: IDetailCardSectionData[] = [
        {
          label: "Skill Statement",
          bodyString: skill.skillStatement,
          showIfEmpty: true
        }, {
          label: "Categories",
          bodyString: skill.categories.join("; "),
          showIfEmpty: true
        }, {
          label: "Keywords",
          bodyString: skill.keywords.join("; "),
          showIfEmpty: true
        },
        {
          label: "Standards",
          bodyString: skill.standards.map(it => it.skillName).join("; "),
          showIfEmpty: true
        }, {
          label: "Certifications",
          bodyString: skill.certifications.map(it => it.name).join("; "),
          showIfEmpty: true
        }, {
          label: "Occupations",
          bodyTemplate: component.occupationsTemplate,
          showIfEmpty: true
        },
        {
          label: "Employers",
          bodyString: skill.employers.map(it => it.name).join("; "),
          showIfEmpty: true
        },
        {
          label: "Alignments",
          bodyString: skill.alignments.map(
            it => `<p class="t-type-body">${it.isPartOf?.name + ": "}<a class="t-link" target="_blank" href="${it.id}">${it.skillName || it.id}</a></p>`
          ).join("; "),
          showIfEmpty: true
        },
        {
          label: "Collections With This RSD",
          bodyString: skill.collections.map(
            it => `<div><a class="t-link" href="/collections/${it.uuid}/manage">${it.name}</a></div>`
          ).join("<br>"),
          showIfEmpty: false
        },
      ]

    // Act
    component.richSkill = new ApiSkill(skill)
    const result: IDetailCardSectionData[] = component.getCardFormat()

    // Assert
    expect(result).toEqual(expected)
  })
})
