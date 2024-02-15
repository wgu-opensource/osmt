import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {AuthService} from "../../auth/auth-service"
import {CategoryService} from "../service/category.service"
import {AuthServiceStub, CategoryServiceStub} from "../../../../test/resource/mock-stubs"
import {KeywordSortOrder} from "../ApiCategory"
import {CategoryTableComponent, getLabelForSortOrder} from "./category-table.component"

@Component({
  template: ""
})
export abstract class TestHostComponent extends CategoryTableComponent {
  execProtected = {}
}

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("CategoryTableComponent", () => {

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CategoryTableComponent,
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
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    activatedRoute.setParams({ userId: 126 })

    // @ts-ignore
    createComponent(TestHostComponent)
  }))

  it("get mobileSortOrderOptions should return correct value", () => {
    expect(component.mobileSortOrderOptions!!.length).toEqual(4)
  })

  it("get mobileSortOrderOptions should return correct value", () => {
    expect(component.mobileSortOrderOption!!.value).toEqual(KeywordSortOrder.KeywordAsc)

    component.sortOrder = KeywordSortOrder.KeywordDesc
    expect(component.mobileSortOrderOption!!.value).toEqual(KeywordSortOrder.KeywordDesc)

    component.sortOrder = KeywordSortOrder.SkillCountAsc
    expect(component.mobileSortOrderOption!!.value).toEqual(KeywordSortOrder.SkillCountAsc)

    component.sortOrder = KeywordSortOrder.SkillCountDesc
    expect(component.mobileSortOrderOption!!.value).toEqual(KeywordSortOrder.SkillCountDesc)
  })

  it("handleSortOrderSelected should succeed", () => {
    expect((() => {
      component.handleSortOrderSelected(KeywordSortOrder.SkillCountDesc)
      return true
    })()).toEqual(true)
  })

  it("handleMobileSortOrderSelected should succeed", () => {
    expect((() => {
      component.handleMobileSortOrderSelected({ label: "", value: KeywordSortOrder.SkillCountDesc })
      return true
    })()).toEqual(true)
  })
})

describe("getLabelForSortOrder", () => {
  it("should return correct values", () => {
    expect(getLabelForSortOrder(KeywordSortOrder.KeywordAsc)).toEqual("Category name (ascending)")
    expect(getLabelForSortOrder(KeywordSortOrder.KeywordDesc)).toEqual("Category name (descending)")
    expect(getLabelForSortOrder(KeywordSortOrder.SkillCountAsc)).toEqual("Skill count (ascending)")
    expect(getLabelForSortOrder(KeywordSortOrder.SkillCountDesc)).toEqual("Skill count (descending)")
  })
})
