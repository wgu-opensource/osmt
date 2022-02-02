import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { FormsModule } from "@angular/forms"
import { Title } from "@angular/platform-browser"
import { NavigationEnd, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { Idle } from "@ng-idle/core"
import { Keepalive, NgIdleKeepaliveModule } from "@ng-idle/keepalive"
import { of } from "rxjs"
import { AppConfig } from "src/app/app.config"
import { EnvironmentService } from "src/app/core/environment.service"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { IdleStub, SearchServiceStub } from "../../test/resource/mock-stubs"
import { RouterStubSpec } from "../../test/util/router-stub.spec"
import { AppComponent } from "./app.component"
import { SearchService } from "./search/search.service"
import { ToastService } from "./toast/toast.service"
import any = jasmine.any


export function createComponent(T: Type<AppComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let routerStub: RouterStubSpec
let component: AppComponent
let fixture: ComponentFixture<AppComponent>


describe("AppComponent construction", () => {
  beforeEach(() => {
    // By using RouterStubSpec, AppComponent.initClearSearchOnNavigate() is covered.
    routerStub = new RouterStubSpec()
  })

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      imports: [
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
        NgIdleKeepaliveModule.forRoot(),
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        Keepalive,
        { provide: Idle, useClass: IdleStub },
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: Router, useValue: routerStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    const url = "/api/collections/search"
    routerStub.setNavigationEnd(new NavigationEnd(0, url, url))

    createComponent(AppComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })
})

describe("AppComponent methods", () => {
  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      imports: [
        FormsModule,  // Required for ([ngModel])
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
        NgIdleKeepaliveModule.forRoot(),
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        Keepalive,
        { provide: Idle, useClass: IdleStub },
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

/* Cannot set color through AppConfig because the required white/black defaults aren't being set.  It's an unused feature.
    const testColor = "#234567"
    AppConfig.settings.colorBrandAccent1 = testColor
*/

    routerSpy.events = of(undefined)  // Component uses events, but it was undefined, so making it an Observable.

    createComponent(AppComponent)
  }))

  it("watchForIdle should return", () => {
    // Arrange
    const idleStub = TestBed.inject(Idle) as IdleStub
    const router = TestBed.inject(Router)

    // Act
    idleStub.onTimeout.next(1)

    // Assert
    expect(router.navigate).toHaveBeenCalledWith([ "/logout" ], any(Object) )
  })

  it("setWhiteLabelColor should return", () => {
    // Arrange
    const testBlack = "#2F2F2F"
    const testWhite = "#CFCFCF"
    const testColor = "#123456"
    const style = document.documentElement.style
    const expected = testColor

    // This tests a function that is not apparently used, and the default styles don't seem to be set either, causing
    //   the method to throw an exception.  So in order to get the test to run, we fix those assumed colors here.
    style.setProperty("--color-onBrandBlack", testBlack)
    style.setProperty("--color-onBrandWhite", testWhite)

    // Act
    component.setWhiteLabelColor(testColor)

    // Assert
    expect(style.getPropertyValue("--color-brand1")).toEqual(expected)
    expect(style.getPropertyValue("--color-a11yOnBrand")).toEqual(testWhite)
  })
})
