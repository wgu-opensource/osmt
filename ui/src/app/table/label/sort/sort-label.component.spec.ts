import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ApiSortOrder} from "../../../richskill/ApiSkill"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {SortLabelComponent} from "./sort-label.component"

@Component({
  template: ""
})
export abstract class TestHostComponent extends SortLabelComponent { }

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("SortLabelComponent", () => {
  const ascSortOrder: any = ApiSortOrder.NameAsc
  const descSortOrder: any = ApiSortOrder.NameDesc

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        SortLabelComponent,
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
    component.selected = undefined
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("get isSortAsc should succeed", () => {
    component.selected = undefined
    expect(component.isAscSelected).toEqual(false)

    component.selected = ascSortOrder
    expect(component.isAscSelected).toEqual(true)

    component.selected = descSortOrder
    expect(component.isAscSelected).toEqual(false)
  })

  it("get isSortDesc should succeed", () => {
    component.selected = undefined
    expect(component.isDescSelected).toEqual(false)

    component.selected = ascSortOrder
    expect(component.isDescSelected).toEqual(false)

    component.selected = descSortOrder
    expect(component.isDescSelected).toEqual(true)

  })

  it("get flipIcon should succeed", () => {
    component.selected = undefined
    expect(component.flipIcon).toEqual(false)

    component.selected = ascSortOrder
    expect(component.flipIcon).toEqual(true)

    component.selected = descSortOrder
    expect(component.flipIcon).toEqual(false)
  })

  it("handleClick should succeed", () => {
    let expected = ascSortOrder
    component.selected = undefined

    component.onSelection.subscribe((e: any) => {
      expect(e).toEqual(expected)
      component.selected = e
      expected = (expected == ascSortOrder) ? descSortOrder : ascSortOrder
    })

    // Check when current not set
    component.handleClick()
    // Check when current is ASC
    component.handleClick()
    // Check when current is DESC
    component.handleClick()
  })
})
