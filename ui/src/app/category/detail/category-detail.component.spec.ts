import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {CategoryDetailComponent} from "./category-detail.component"
import {ApiCategory} from "../ApiCategory"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {PublishStatus} from "../../PublishStatus"
import {FilterDropdown} from "../../models/filter-dropdown.model"
import {AuthService} from "../../auth/auth-service"
import {AuthServiceStub, CategoryServiceData,CategoryServiceStub} from "../../../../test/resource/mock-stubs"
import {CategoryService} from "../service/category.service"

@Component({
  template: ""
})
export abstract class TestHostComponent extends CategoryDetailComponent {
  execProtected = {
    searchFieldValue: () => this.searchFieldValue,
    clearCategory: () => this.clearCategory(),
    clearSkills: () => this.clearSkills(),
    setCategory: (c: ApiCategory) => this.setCategory(c),
    loadCategory: () => this.loadCategory(),
    loadSkills: () => this.loadSkills(),
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

describe("CategoryDetailComponent", () => {

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CategoryDetailComponent,
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

  it("get showLibraryEmptyMessage should return correct result", () => {
    expect(component.showLibraryEmptyMessage).toEqual(true)
  })

  it("get showSkillsEmpty should return correct result", () => {
    expect(component.showSkillsEmpty).toEqual(true)

    component.skillTableControl.loadSkills(1)
    expect(component.showSkillsEmpty).toEqual(false)
  })

  it("get showSkillsFilters should return correct result", () => {
    expect(component.showSkillsFilters).toEqual(true)
  })

  it("get showSkillsLoading should return correct result", () => {
    expect(component.showSkillsLoading).toEqual(false)
  })

  it("get showSkillsTable should return correct result", () => {
    expect(component.showSkillsTable).toEqual(false)

    component.skillTableControl.loadSkills(1)
    expect(component.showSkillsTable).toEqual(true)
  })

  it("get skillsCountLabel should return correct result", () => {
    expect(component.skillsCountLabel).toEqual("0 RSDs with this category based on")

    component.skillTableControl.loadSkills(1)
    expect(component.skillsCountLabel)
      .toEqual(`${component.skillTableControl.totalCount} RSDs with this category based on`)
  })

  it("get skillsViewingLabel should return correct result", () => {
    expect(component.skillsViewingLabel).toEqual("")

    component.skillTableControl.loadSkills(1)
    expect(component.skillsViewingLabel).toEqual("")
  })

  it("get mobile sort options should return only one set of options", () => {
    expect(Object.keys(component.getMobileSortOptions()).length).toEqual(2);
  })

  it("get tableActions should return correct result", () => {
    expect(component.tableActions.length).toEqual(1)
  })

  it("get searchFieldValue should return correct result", () => {
    expect(component.execProtected.searchFieldValue()).toBeUndefined()

    const query = "abc123"
    component.searchForm.setValue({ search: query })
    expect(component.execProtected.searchFieldValue()).toEqual(query)
  })

  it("ngOnInit should succeed", () => {
    expect((() => {
      component.ngOnInit()
      return true
    })()).toEqual(true)
  })

  it("navigateToPage should succeed", () => {
    expect(component.skillTableControl.currPageNumber).toEqual(1)

    component.navigateToPage(2)
    expect(component.skillTableControl.currPageNumber).toEqual(2)
  })

  it("clearSearch should succeed", () => {
    const query = "abc123"
    component.searchForm.setValue({ search: query })
    expect(component.execProtected.searchFieldValue()).toBeDefined()

    component.clearSearch()
    expect(component.execProtected.searchFieldValue()).toBeUndefined()
  })

  it("clearCategory should succeed", () => {
    component.category = CategoryServiceData.category
    expect(component.category).toBeDefined()

    component.execProtected.clearCategory()
    expect(component.category).toBeUndefined()
  })

  it("clearSkills should succeed", () => {
    component.skillTableControl.loadSkills(1)
    expect(component.skillTableControl.hasResults).toEqual(true)

    component.execProtected.clearSkills()
    expect(component.skillTableControl.hasResults).toEqual(false)
  })

  it("setCategory should succeed", () => {
    expect(component.category).toBeUndefined()

    component.execProtected.setCategory(CategoryServiceData.category)
    expect(component.category).toEqual(CategoryServiceData.category)
  })

  it("loadCategory should succeed", () => {
    expect(component.category).toBeUndefined()

    component.execProtected.loadCategory()
    expect(component.category).toBeUndefined()
  })

  it("loadSkills should succeed", () => {
    expect(component.skillTableControl.hasResults).toEqual(false)

    component.category = CategoryServiceData.category
    component.execProtected.loadSkills()
    expect(component.skillTableControl.hasResults).toEqual(true)
  })

  it("handleHeaderColumnSort should succeed", () => {
    expect((() => {
      component.handleHeaderColumnSort(ApiSortOrder.SkillAsc)
      return true
    })()).toEqual(true)
  })

  it("handlePageClicked should succeed", () => {
    expect((() => {
      component.handlePageClicked(1)
      return true
    })()).toEqual(true)
  })

  it("handleStatusFilterChange should succeed", () => {
    expect((() => {
      component.handleStatusFilterChange(new Set<PublishStatus>([PublishStatus.Published]))
      return true
    })()).toEqual(true)
  })

  it("handleKeywordFilterChange should succeed", () => {
    expect((() => {
      component.handleKeywordFilterChange({} as FilterDropdown)
      return true
    })()).toEqual(true)
  })

  it("handleSearchSubmit should succeed", () => {
    expect((() => {
      component.handleSearchSubmit()
      return true
    })()).toEqual(true)
  })
})
