import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {LabelWithSortComponent} from "./label-with-sort.component"



@Component({
  template: ""
})
export abstract class TestHostComponent extends LabelWithSortComponent { }

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("LabelWithSortComponent", () => {
  const ascSortOrder: any = ApiSortOrder.NameAsc
  const descSortOrder: any = ApiSortOrder.NameDesc

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        LabelWithSortComponent,
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

    component.sortOrderAsc = ascSortOrder
    component.sortOrderDesc = descSortOrder
    component.currentSortOrder = undefined
  }))

  it("get isSortAsc should succeed", () => {
    component.currentSortOrder = undefined
    expect(component.isSortAsc).toEqual(false)

    component.currentSortOrder = ascSortOrder
    expect(component.isSortAsc).toEqual(true)

    component.currentSortOrder = descSortOrder
    expect(component.isSortAsc).toEqual(false)
  })

  it("get isSortDesc should succeed", () => {
    component.currentSortOrder = undefined
    expect(component.isSortDesc).toEqual(false)

    component.currentSortOrder = ascSortOrder
    expect(component.isSortDesc).toEqual(false)

    component.currentSortOrder = descSortOrder
    expect(component.isSortDesc).toEqual(true)

  })

  it("get flipIcon should succeed", () => {
    component.currentSortOrder = undefined
    expect(component.flipIcon).toEqual(false)

    component.currentSortOrder = ascSortOrder
    expect(component.flipIcon).toEqual(true)

    component.currentSortOrder = descSortOrder
    expect(component.flipIcon).toEqual(false)
  })

  it("onClick should succeed", () => {
    let expected = ascSortOrder
    component.currentSortOrder = undefined

    component.sortOrderSelected.subscribe((e: any) => {
      expect(e).toEqual(expected)
      component.currentSortOrder = e
      expected = (expected == ascSortOrder) ? descSortOrder : ascSortOrder
    })

    // Check when current not set
    component.onClick()
    // Check when current is ASC
    component.onClick()
    // Check when current is DESC
    component.onClick()
  })
})
