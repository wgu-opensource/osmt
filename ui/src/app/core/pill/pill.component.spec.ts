// noinspection LocalVariableNamingConventionJS
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {PillComponent} from "./pill.component";
import {TestPillControl} from "./pill-control.spec";
import {AbstractPillControl} from "./pill-control";

@Component({
  template: "",
})
export abstract class TestHostComponent extends PillComponent<AbstractPillControl> {
  constructor(pillControl: TestPillControl = new TestPillControl()) {
    super();
    this.control = pillControl
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

describe("PillComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        PillComponent,
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

  it("get isSelelected should return correct result", () => {
    expect(component.isSelected).toEqual(false)
    component.control?.select()
    expect(component.isSelected).toEqual(true)
  })

  it("get hasClickedObservers should return correct result", () => {
    expect(component.hasClickedObservers).toEqual(false)
  })

  it("get hasSecondaryLabel should return correct result", () => {
    expect(component.hasSecondaryLabel).toEqual(true)
  })

  it("get primaryLabel should return correct result", () => {
    expect(component.primaryLabel).toEqual(TestPillControl.TEST_PRIMARY_STRING)
  })

  it("get secondaryLabel should return correct result", () => {
    expect(component.secondaryLabel).toEqual(TestPillControl.TEST_SECONDARY_STRING)
  })

  it("onClick should succeed", () => {
    expect((() => {
      component.onClick()
      return true
    })()).toEqual(true)
  })
})
