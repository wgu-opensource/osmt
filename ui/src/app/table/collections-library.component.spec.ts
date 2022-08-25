import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { RouterTestingModule } from "@angular/router/testing"
import { createMockCollectionSummary } from "../../../test/resource/mock-data"
import {AuthServiceStub, CollectionServiceStub} from "../../../test/resource/mock-stubs"
import { AppConfig } from "../app.config"
import { CollectionService } from "../collection/service/collection.service"
import { EnvironmentService } from "../core/environment.service"
import { PaginatedCollections } from "../richskill/service/rich-skill-search.service"
import { ToastService } from "../toast/toast.service"
import { CollectionsLibraryComponent } from "./collections-library.component"
import {AuthService} from "../auth/auth-service";


export function createComponent(T: Type<CollectionsLibraryComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: CollectionsLibraryComponent
let fixture: ComponentFixture<CollectionsLibraryComponent>


describe("CollectionsLibraryComponent", () => {
  let collectionService: CollectionService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        CollectionsLibraryComponent
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,  // Required for routerLink
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        Title,
        ToastService,
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    collectionService = TestBed.inject(CollectionService)
    createComponent(CollectionsLibraryComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  // Other cases were already covered by other tests
  it("nextLoadPage should setResults", () => {
    // Arrange
    component.selectedFilters = new Set([])
    component.results = undefined as unknown as PaginatedCollections
    component.selectedCollections = [ createMockCollectionSummary()]
    const emptyPage = new PaginatedCollections([], 0)

    // Act
    component.loadNextPage()

    // Assert
    expect(component.results).toEqual(emptyPage)
  })
})
