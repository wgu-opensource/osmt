import { HttpClientModule } from "@angular/common/http"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ReactiveFormsModule } from "@angular/forms"
import { Router } from "@angular/router"
import { EnvironmentServiceStub, SearchHubServiceStub } from "test/resource/mock-stubs"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { AppConfig } from "../../app.config"
import { EnvironmentService } from "../../core/environment.service"
import { FormFieldText } from "../../form/form-field-text.component"
import { FormField } from "../../form/form-field.component"
import { AdvancedSearchHorizontalActionBarComponent } from "./action-bar/advanced-search-horizontal-action-bar.component"
import { AdvancedSearchVerticalActionBarComponent } from "./action-bar/advanced-search-vertical-action-bar.component"
import { AdvancedSearchComponent } from "./advanced-search.component"
import {SearchHubService} from "../searchhub/searchhub.service";


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
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },  // Example of using a service stub
        { provide: Router, useValue: routerSpy },
        { provide: SearchHubService, useClass: SearchHubServiceStub }
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    activatedRoute.setParams({ userId: 126 })
    createComponent(AdvancedSearchComponent)
  }))

  it("AdvancedSearchComponent should be created", () => {
    expect(component).toBeTruthy()
  })
})
