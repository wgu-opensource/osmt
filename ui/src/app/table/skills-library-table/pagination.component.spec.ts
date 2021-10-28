import {Component, Type} from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import {PaginationComponent} from "./pagination.component"
import {FormsModule} from "@angular/forms"
import {RouterTestingModule} from "@angular/router/testing"
import {ActivatedRoute, Router} from "@angular/router"
import {EnvironmentService} from "../../core/environment.service"


@Component({
  template: `
    <app-pagination [currentPage]="currentPage" [totalPages]="totalPages">
    </app-pagination>`
})
class TestHostComponent {
  currentPage = 1
  totalPages = 1
}

export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  hostComponent = fixture.componentInstance

  const debugEl = fixture.debugElement.query(By.directive(PaginationComponent))
  childComponent = debugEl.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let activatedRoute: ActivatedRouteStubSpec
let hostComponent: TestHostComponent
let childComponent: PaginationComponent
let fixture: ComponentFixture<TestHostComponent>


describe("PaginationComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        PaginationComponent,
        TestHostComponent
      ],
      imports: [
        FormsModule,  // Required for ([ngModel])
        RouterTestingModule  // Required for routerLink
      ],
      providers: [
        EnvironmentService,
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()
    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(childComponent).toBeTruthy()
    expect(hostComponent).toBeTruthy()
  })

  it("currentPage should stay updated with correct values", () => {
    expect(childComponent.currentPage).toEqual(1)

    hostComponent.currentPage = 5
    fixture.detectChanges()
    expect(childComponent.currentPage).toEqual(5)
  })

  it("pageNumbers should return an array with all page numbers (middle)", () => {
    expect(childComponent.pageNumbers()).toEqual([1, 1])
    hostComponent.currentPage = 8
    hostComponent.totalPages = 20
    fixture.detectChanges()
    expect(childComponent.pageNumbers(2, 2)).toEqual([1, NaN, 6, 7, 8, 9, 10, NaN, 20])
  })
  it("pageNumbers should return an array with all page numbers (lead)", () => {
    expect(childComponent.pageNumbers()).toEqual([1, 1])
    hostComponent.currentPage = 1
    hostComponent.totalPages = 3
    fixture.detectChanges()
    expect(childComponent.pageNumbers(2, 2)).toEqual([1, 2, 3])
  })
  it("pageNumbers should return an array with all page numbers (trail)", () => {
    expect(childComponent.pageNumbers()).toEqual([1, 1])
    hostComponent.currentPage = 5
    hostComponent.totalPages = 5
    fixture.detectChanges()
    expect(childComponent.pageNumbers(2, 2)).toEqual([1, NaN, 3, 4, 5])
  })

  it("isEllipsis should return correctly", () => {
    expect(childComponent.isEllipsis(5)).toBeFalse()
    expect(childComponent.isEllipsis(Number("..."))).toBeTrue()
  })

  it("isCurrentPage should return the proper page", () => {
    expect(childComponent.isCurrentPage(5)).toBeFalse()
    hostComponent.currentPage = 5
    fixture.detectChanges()
    expect(childComponent.isCurrentPage(5)).toBeTrue()
  })

  it("isPrevDisabled should properly return either an empty string or a null value", () => {
    expect(childComponent.isPrevDisabled()).toEqual("")
    hostComponent.currentPage = 2
    hostComponent.totalPages = 5
    fixture.detectChanges()
    expect(childComponent.isPrevDisabled()).toEqual(null)
  })

  it("isNextDisabled should properly return either an empty string or a null value", () => {
    expect(childComponent.isNextDisabled()).toEqual("")
    hostComponent.currentPage = 1
    hostComponent.totalPages = 5
    fixture.detectChanges()
    expect(childComponent.isNextDisabled()).toEqual(null)
  })

  it("handleClickPrev should return previous page number", () => {
    hostComponent.currentPage = 5
    fixture.detectChanges()
    spyOn(childComponent.pageClicked, "emit")
    childComponent.handleClickPrev()
    fixture.detectChanges()
    expect(childComponent.pageClicked.emit).toHaveBeenCalledWith(4)
  })

  it("handleClickNext should return next page number", () => {
    spyOn(childComponent.pageClicked, "emit")
    childComponent.handleClickNext()
    // expect(childComponent.handleClickPrev()).toBeFalse()
    fixture.detectChanges()
    expect(childComponent.pageClicked.emit).toHaveBeenCalledWith(2)
    expect(childComponent.handleClickNext()).toBeFalse()
  })
})
