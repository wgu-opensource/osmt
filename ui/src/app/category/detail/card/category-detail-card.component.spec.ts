import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {AuthService} from "../../../auth/auth-service"
import {AuthServiceStub, CategoryServiceData, CategoryServiceStub} from "../../../../../test/resource/mock-stubs"
import {CategoryService} from "../../service/category.service"
import {CategoryDetailCardComponent} from "./category-detail-card.component"
import { getBaseApi } from "../../../api-versions"

@Component({
  template: ""
})
export abstract class TestHostComponent extends CategoryDetailCardComponent { }

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("CategoryDetailCardComponent", () => {

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CategoryDetailCardComponent,
        TestHostComponent
      ],
      imports: [
        FormsModule,  // Required for ([ngModel])
        ReactiveFormsModule,
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        AppConfig,  // Needed to avoid the toolName race condition below
        EnvironmentService,  // Needed to avoid the toolName race condition below
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: CategoryService, useClass: CategoryServiceStub },
        { provide: Router, useValue: routerSpy },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    activatedRoute.setParams({ userId: 126 })

    // @ts-ignore
    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("get categoryName should return correct result", () => {
    expect(component.categoryName).toEqual("")

    component.category = CategoryServiceData.category
    expect(component.categoryName).toEqual(CategoryServiceData.category.name)
  })

  it("get categorySkillsLabel should return correct result", () => {
    expect(component.categorySkillsLabel).toEqual("")

    component.showSkillCount = true
    expect(component.categorySkillsLabel).toEqual("")

    component.category = CategoryServiceData.category
    expect(component.categorySkillsLabel).toEqual(`${CategoryServiceData.category.skillCount} RSDs with category.`)

    const index = 20
    const currOnPage = 10
    component.indexOfFirstSkill = index
    component.currentOnPage = currOnPage
    expect(component.categorySkillsLabel)
      .toEqual(`${CategoryServiceData.category.skillCount} RSDs with category. Viewing ${component.firstSkillIndex}-${component.lastSkillIndex}.`)
  })

  it("get categorySkillCount should return correct result", () => {
    expect(component.categorySkillCount).toBeUndefined()

    component.category = CategoryServiceData.category
    expect(component.categorySkillCount).toEqual(`${CategoryServiceData.category.skillCount}`)
  })

  it("get firstSkillIndex should return correct result", () => {
    expect(component.firstSkillIndex).toBeUndefined()

    const index = 20
    component.indexOfFirstSkill = index
    expect(component.firstSkillIndex).toEqual(`${index + 1}`)
  })

  it("get lastSkillIndex should return correct result", () => {
    expect(component.lastSkillIndex).toBeUndefined()

    const index = 20
    const currOnPage = 10
    component.indexOfFirstSkill = index
    component.currentOnPage = currOnPage
    expect(component.lastSkillIndex).toEqual(`${index + currOnPage}`)
  })
})
