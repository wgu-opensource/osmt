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
import {AuthServiceStub, CategoryServiceData, CategoryServiceStub} from "../../../../test/resource/mock-stubs"
import {KeywordSortOrder, PaginatedCategories} from "../ApiCategory"
import {CategoryListComponent} from "./category-list.component"


@Component({
  template: ""
})
export abstract class TestHostComponent extends CategoryListComponent {
  execProtected = {
    setResults: (r: PaginatedCategories | undefined) => this.setResults(r)
  }
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

describe("CategoryListComponent", () => {

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CategoryListComponent,
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

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("get categoryCountLabel should return correct result", () => {
    expect(component.categoryCountLabel).toEqual("")

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.categoryCountLabel).toEqual("")
  })

  it("get firstRecordNo should return correct result", () => {
    expect(component.firstRecordNo).toEqual(1)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.firstRecordNo).toEqual(1)
  })

  it("get lastRecordNo should return correct result", () => {
    expect(component.lastRecordNo).toEqual(1)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.lastRecordNo).toEqual(1)
  })

  it("get currPageNo should return correct result", () => {
    expect(component.currPageNo).toEqual(1)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.currPageNo).toEqual(1)
  })

  it("get currPageCount should return correct result", () => {
    expect(component.currPageCount).toEqual(0)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.currPageCount).toEqual(1)
  })

  it("get totalCount should return correct result", () => {
    expect(component.totalCount).toEqual(0)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.totalCount).toEqual(0)
  })

  it("get totalPageCount should return correct result", () => {
    expect(component.totalPageCount).toEqual(0)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.totalPageCount).toEqual(1)
  })

  it("get hasResults should return correct result", () => {
    expect(component.hasResults).toEqual(false)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.hasResults).toEqual(false)
  })

  it("get emptyResults should return correct result", () => {
    expect(component.emptyResults).toEqual(true)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(component.emptyResults).toEqual(false)
  })

  it("get tableActions should return correct result", () => {
    expect(component.tableActions.length).toEqual(1)
  })

  it("getMobileSortOptions should return correct result", () => {
    expect(component.getMobileSortOptions()).toBeTruthy()
  })

  it("setResults should succeed", () => {
    expect(this.hasResults()).toEqual(true)

    this.execProtected.setResults(CategoryServiceData.paginatedCategories)
    expect(this.hasResults()).toEqual(true)

    this.execProtected.setResults(undefined)
    expect(this.hasResults()).toEqual(false)

  })

  it("loadNextPage should succeed", () => {
    expect(this.hasResults()).toEqual(false)

    this.loadNextPage()
    expect(this.hasResults()).toEqual(true)
  })

  it("navigateToPage should succeed", () => {
    expect(this.currPageNo).toEqual(1)

    this.navigateToPage(2)
    expect(this.currPageNo).toEqual(2)
  })

  it("handleHeaderColumnSort should succeed", () => {
    expect((() => {
      component.handleHeaderColumnSort(KeywordSortOrder.SkillCountDesc)
      return true
    })()).toEqual(true)
  })
})
