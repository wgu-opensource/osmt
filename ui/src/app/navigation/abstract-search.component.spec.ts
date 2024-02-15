import { HttpClientModule } from "@angular/common/http"
import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ActivatedRoute, Router } from "@angular/router"
import { AuthServiceStub, SearchServiceStub } from "../../../test/resource/mock-stubs"
import { ActivatedRouteStubSpec } from "../../../test/util/activated-route-stub.spec"
import { SearchService } from "../search/search.service"
import { AbstractSearchComponent } from "./abstract-search.component"
import {AuthService} from "../auth/auth-service"


@Component({
  selector: "app-advanced-search",
  template: ``
})
export class ConcreteSearchComponent extends AbstractSearchComponent {
  constructor(searchService: SearchService, route: ActivatedRoute, authService: AuthService) {
    super(searchService, route, authService)
  }
}


let component: ConcreteSearchComponent
let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<ConcreteSearchComponent>


export function createComponent(T: Type<ConcreteSearchComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}

describe("AbstractSearchComponent", () => {
  const search = "testSearchQuery"

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })
  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        ConcreteSearchComponent
      ],
      imports: [
        HttpClientModule
      ],
      providers: [
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
      .compileComponents()

    activatedRoute.setParams({ userId: 126 })
    createComponent(ConcreteSearchComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("constructor searchQuery updates correctly", () => {
    // Arrange
    component.searchForm.setValue({search})
    expect(component.searchQuery).toEqual(search)
    const searchService = TestBed.inject(SearchService);

    // Act
    (searchService as unknown as SearchServiceStub).setLatestSearch(undefined)

    // Assert
    while (component.searchQuery !== ""){}
    expect(component.searchQuery).toEqual("")
  })

  it("clearSearch should be correct", () => {
    component.searchForm.setValue({search})
    expect(component.searchQuery).toEqual(search)
    expect(component.clearSearch()).toBeFalse()
    expect(component.searchQuery).toEqual("")
  })

  it("handleDefaultSubmit should be false", () => {
    component.searchForm.setValue({search: ""})
    expect(component.handleDefaultSubmit()).toBeFalse()
    expect(component.searchQuery).toEqual("")
    component.searchForm.setValue({search})
    expect(component.handleDefaultSubmit()).toBeFalse()
    expect(component.searchQuery).toEqual(search)
  })

  it("submitCollections should be false", () => {
    component.clearSearch()
    expect(component.submitCollectionSearch()).toBeFalse()
    expect(component.searchQuery).toEqual("")
    component.searchForm.setValue({search})
    expect(component.submitCollectionSearch()).toBeFalse()
    expect(component.searchQuery).toEqual(search)
  })

})
