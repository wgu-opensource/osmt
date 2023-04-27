import { Location } from "@angular/common"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { RouterTestingModule } from "@angular/router/testing"
import { createMockPaginatedSkills, createMockSkillSummary } from "../../../test/resource/mock-data"
import { AuthServiceStub, CollectionServiceStub, RichSkillServiceStub } from "../../../test/resource/mock-stubs"
import { AppConfig } from "../app.config"
import { EnvironmentService } from "../core/environment.service"
import { ApiSearch, PaginatedSkills } from "../richskill/service/rich-skill-search.service"
import { RichSkillService } from "../richskill/service/rich-skill.service"
import { TableActionDefinition } from "../table/skills-library-table/has-action-definitions"
import { ToastService } from "../toast/toast.service"
import { CollectionSkillSearchComponent } from "./collection-skill-search.component"
import { CollectionService } from "./service/collection.service"
import {AuthService} from "../auth/auth-service";


export function createComponent(T: Type<CollectionSkillSearchComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: CollectionSkillSearchComponent
let fixture: ComponentFixture<CollectionSkillSearchComponent>


describe("CollectionSkillSearchComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        CollectionSkillSearchComponent
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        Title,
        Location,
        ToastService,
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    createComponent(CollectionSkillSearchComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("handleDefaultSubmit should be correct", () => {
    // Arrange
    spyOn(component, "loadNextPage")
    component.from = 1

    // Act
    component.handleDefaultSubmit()

    // Assert
    expect(component.from).toEqual(0)
    expect(component.loadNextPage).toHaveBeenCalled()
  })

  it("loadNextPage should load none", () => {
    // Arrange
    const search = ""
    component.searchForm.setValue({search})

    // Act
    component.loadNextPage()

    // Assert
    expect(component.results).toEqual(new PaginatedSkills([], 0))
  })

  it("loadNextPage should load some", () => {
    // Arrange
    const search = "testSearchValue"
    component.searchForm.setValue({search})

    // Act
    component.loadNextPage()

    // Assert
    expect(component.results).toEqual(createMockPaginatedSkills())
  })

  it("clearSearch should reset form", () => {
    // Arrange
    spyOn(component.searchForm, "reset")

    // Act
    const result = component.clearSearch()

    // Assert
    expect(result).toBeFalse()
    expect(component.searchForm.reset).toHaveBeenCalled()
  })

  it("rowActions should return 1", () => {
    // Arrange
    const expected = new TableActionDefinition({
      label: "Add to Collection",
      callback: undefined
    })

    // Act
    const result = component.rowActions()

    // Assert
    expect(result[0].label).toEqual(expected.label)
  })

  it("actionsVisible should be correct", () => {
    // Arrange
    component.results = undefined
    // Act/Assert
    expect(component.actionsVisible()).toBeFalse()

    // Arrange
    component.results = new PaginatedSkills([], 0)
    // Act/Assert
    expect(component.actionsVisible()).toBeTrue()
  })

  it("tableActions should return 2", () => {
    // Arrange
    const expected = [
      new TableActionDefinition({
        label: "Back to Top",
        icon: "up",
        offset: true,
        callback: undefined
      }),
      new TableActionDefinition({
        label: "Add to Collection",
        icon: "collection",
        primary: true,
        callback: undefined,
        visible: undefined
      })
    ]

    // Act
    const result = component.tableActions()

    // Assert
    expect(result.length).toEqual(expected.length)
    for (let j = 0; j < expected.length; ++j) {
      expect(result[j].label).toEqual(expected[j].label)
      expect(result[j].icon).toEqual(expected[j].icon)
      expect(result[j].offset).toEqual(expected[j].offset)
      expect(result[j].primary).toEqual(expected[j].primary)
    }
  })

  it("handleClickAddCollection should return", () => {
    // Arrange
    const row = component.rowActions()[0]
    const skill = createMockSkillSummary()

    // Act
    let result
    if (row.callback) {
      result = row.callback(row, skill)
    }

    // Assert
    expect(result).toBeFalse()
  })

  it("getApiSearch should return canned search", () => {
    // Arrange
    component.selectAllChecked = true
    const search = "testQueryString"
    component.searchForm.setValue({search})

    // Act
    const result = component.getApiSearch()

    // Assert
    expect(result).toEqual(new ApiSearch({ query: search }))
  })

  it("getApiSearch should return specified search", () => {
    // Arrange
    component.selectAllChecked = false
    component.selectedSkills = [createMockSkillSummary("1")]
    const result = component.selectedUuids()

    // Assert
    expect(result).toEqual(component.getApiSearch()?.uuids)
  })

  it("handleSelectAll should be correct", () => {
    // Arrange
    component.results = new PaginatedSkills([], 10)
    component.size = 5

    // Act
    component.handleSelectAll(true)
    // Assert
    expect(component.selectAllChecked).toBeTrue()

    // Arrange
    component.results = new PaginatedSkills([], 5)
    component.size = 5

    // Act
    component.handleSelectAll(true)
    // Assert
    expect(component.multiplePagesSelected).toBeFalse()
  })
})
