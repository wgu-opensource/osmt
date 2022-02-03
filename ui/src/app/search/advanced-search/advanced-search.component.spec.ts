import { HttpClientModule } from "@angular/common/http"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ReactiveFormsModule } from "@angular/forms"
import { Router } from "@angular/router"
import { EnvironmentServiceStub, SearchServiceData, SearchServiceStub } from "test/resource/mock-stubs"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { AppConfig } from "../../app.config"
import { EnvironmentService } from "../../core/environment.service"
import { FormFieldText } from "../../form/form-field-text.component"
import { FormField } from "../../form/form-field.component"
import { INamedReference } from "../../richskill/ApiSkill"
import { ApiSearch } from "../../richskill/service/rich-skill-search.service"
import { SearchService } from "../search.service"
import { AdvancedSearchHorizontalActionBarComponent } from "./action-bar/advanced-search-horizontal-action-bar.component"
import { AdvancedSearchVerticalActionBarComponent } from "./action-bar/advanced-search-vertical-action-bar.component"
import { AdvancedSearchComponent } from "./advanced-search.component"


export function createComponent(T: Type<AdvancedSearchComponent>): Promise<void> {
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
let component: AdvancedSearchComponent
let fixture: ComponentFixture<AdvancedSearchComponent>


describe("AdvancedSearchComponent", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AdvancedSearchComponent,
        AdvancedSearchHorizontalActionBarComponent,
        AdvancedSearchVerticalActionBarComponent,
        FormField,
        FormFieldText
      ],
      imports: [
        ReactiveFormsModule,
        HttpClientModule
      ],
      providers: [
        AppConfig,
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },  // Example of using a service stub
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    activatedRoute.setParams({ userId: 126 })
    createComponent(AdvancedSearchComponent)

    searchService = TestBed.inject(SearchService)
  }))

  it("AdvancedSearchComponent should be created", () => {
    expect(component).toBeTruthy()
  })

  it("handleSearchSkills should return", () => {
    // Arrange
    SearchServiceData.latestSearch = new ApiSearch({})  // Clear previous search
    const {
      name,
      author,
      skillStatement,
      category,
      keywords,
      standards,
      certifications,
      occupations,
      employers,
      alignments,
      collectionName
      // tslint:disable-next-line:no-any
    } = setupForm(false) as any

    const advanced = {
      skillName: name,
      author,
      skillStatement,
      category,
      keywords: tokenizeString(keywords),
      standards: prepareNamedReferences(standards),
      certifications: prepareNamedReferences(certifications),
      occupations: tokenizeString(occupations),
      employers: prepareNamedReferences(employers),
      alignments: prepareNamedReferences(alignments),
      collectionName
    }
    const expected = new ApiSearch({ advanced })

    // Act
    component.handleSearchSkills()

    // Assert
    expect(SearchServiceData.latestSearch).toEqual(expected)
  })
})

function setupForm(isBlank: boolean): object {
  const form = component.skillForm
  const fields = isBlank
    ? {
      name: "",
      skillStatement: "",
      author: "",
      category: "",
      keywords: [].join(";"),
      standards: [].join(";"),
      certifications: [].join(";"),
      occupations: [].join(";"),
      employers: [].join(";"),
      alignments: [].join(";"),
      collectionName: ""
    }
    : {
      name: "my skill",
      skillStatement: "my statement",
      author: "my author",
      category: "my category",
      keywords: ["keyword1", "keyword2", "keyword3"].join(";"),
      standards: ["standard1", "standard2", "standard3"].join(";"),
      certifications: ["certification1", "certification2", "certification3"].join(";"),
      occupations: ["occupation1", "occupation2", "occupation3"].join(";"),
      employers: ["employer1", "employer2", "employer3"].join(";"),
      alignments: ["alignment1", "alignment2"].join(";"),
      collectionName: "collection 1"
    }
  form.setValue(fields)
  return fields
}

function tokenizeString(s: string, token: string = ";"): string[] | undefined {
  return s
      .split(token)
      .map(v => v.trim())
      .filter(v => v.length > 0)
    || undefined
}

function prepareNamedReferences(value: string, token: string = ";"): INamedReference[] | undefined {
  return tokenizeString(value, token)?.map(v => ({name: v})) || undefined
}
