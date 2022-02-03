// noinspection LocalVariableNamingConventionJS

import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"
import { By } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { first } from "rxjs/operators"
import { AppConfig } from "src/app/app.config"
import { EnvironmentService } from "src/app/core/environment.service"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { createMockJobcode } from "../../../../../test/resource/mock-data"
import { KeywordSearchServiceStub } from "../../../../../test/resource/mock-stubs"
import { ApiJobCode } from "../../../job-codes/Jobcode"
import { KeywordSearchService } from "../../../richskill/service/keyword-search.service"
import { FormFieldSearchSelectJobcodeComponent } from "./form-field-search-select-jobcode.component"


let hostSelectedCodes: string[] | undefined = []

@Component({
  template: `
    <app-form-field-search-select-jobcode
      [existing]="myExisting"
      (currentSelection)="handleOutput($event)"
    ></app-form-field-search-select-jobcode>`
})
class TestHostComponent {
  myExisting = []

  handleOutput(codes: string[]): void {
    hostSelectedCodes = codes
  }
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(FormFieldSearchSelectJobcodeComponent))
  childComponent = debugEl.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  hostFixture.detectChanges()

  return hostFixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    hostFixture.detectChanges()
  })
}


let activatedRoute: ActivatedRouteStubSpec
let hostFixture: ComponentFixture<TestHostComponent>
let hostComponent: TestHostComponent
let childComponent: FormFieldSearchSelectJobcodeComponent


describe("FormFieldSearchSelectJobcodeComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        FormFieldSearchSelectJobcodeComponent,
        TestHostComponent
      ],
      imports: [
        FormsModule,  // Required for ([ngModel])
        ReactiveFormsModule,
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        { provide: KeywordSearchService, useClass: KeywordSearchServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    activatedRoute.setParams({ userId: 126 })
    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
  })

  it("isResultSelected should return", () => {
    // Arrange
    const apiJobCode = new ApiJobCode(createMockJobcode())
    childComponent.internalSelectedResults = [ apiJobCode ]

    // Act
    const result = childComponent.isResultSelected(apiJobCode)

    // Assert
    expect(result).toBeTruthy()
  })
  it("isResultSelected should not return", () => {
    // Arrange
    const apiJobCode = new ApiJobCode(createMockJobcode())
    childComponent.internalSelectedResults = []

    // Act
    const result = childComponent.isResultSelected(apiJobCode)

    // Assert
    expect(result).toBeFalse()
  })

  it("performInitialSearchAndPopulation should return", () => {
    // Arrange
    childComponent.internalSelectedResults = []
    const jobCode1 = createMockJobcode(1)
    const jobCode2 = createMockJobcode(2)
    childComponent.existing = [ jobCode1, jobCode2 ]

    // Act
    childComponent.performInitialSearchAndPopulation()

    // Assert
    const apiJobCode1 = new ApiJobCode(jobCode1)
    const apiJobCode2 = new ApiJobCode(jobCode2)
    expect(childComponent.internalSelectedResults.length).toEqual(2)
    expect(childComponent.internalSelectedResults).toEqual([ apiJobCode1, apiJobCode2 ])
  })

  it("performSearch should return", () => {
    // Arrange
    childComponent.results = undefined

    // Act
    childComponent.performSearch("one")

    // Assert
    expect(childComponent.results).toBeTruthy()
    if (childComponent.results) {
      const results: ApiJobCode[] = childComponent.results
      expect(results.length).toEqual(1)
      expect(results[0].targetNodeName).toEqual("one")
    }
  })
  it("performSearch should work even with a prior search", () => {
    // Arrange
    childComponent.results = undefined

    // Act
    childComponent.performSearch("one")
    childComponent.performSearch("two")

    // Assert
    expect(childComponent.results).toBeTruthy()
    if (childComponent.results) {
      const results: ApiJobCode[] = childComponent.results
      expect(results.length).toEqual(0)
    }
  })

  it("selectResult should return", () => {
    // Arrange
    const apiJobCode = new ApiJobCode(createMockJobcode())
    childComponent.internalSelectedResults = [ ]

    // Act
    childComponent.selectResult(apiJobCode)

    // Assert
    expect(childComponent.internalSelectedResults).toEqual([ apiJobCode ])
  })
  it("selectResult should return (no change)", () => {
    // Arrange
    const apiJobCode = new ApiJobCode(createMockJobcode())
    childComponent.internalSelectedResults = [ apiJobCode ]

    // Act
    childComponent.selectResult(apiJobCode)

    // Assert
    expect(childComponent.internalSelectedResults).toEqual([ apiJobCode ])
  })

  it("unselectResult should return", () => {
    // Arrange
    const apiJobCode = new ApiJobCode(createMockJobcode())
    childComponent.internalSelectedResults = [ apiJobCode ]

    // Act
    childComponent.unselectResult(apiJobCode)

    // Assert
    expect(childComponent.internalSelectedResults).toEqual([ ])
  })
  it("unselectResult should return (no change)", () => {
    // Arrange
    const apiJobCode1 = new ApiJobCode(createMockJobcode())
    const apiJobCode2 = new ApiJobCode(createMockJobcode())
    childComponent.internalSelectedResults = [ apiJobCode1 ]

    // Act
    childComponent.unselectResult(apiJobCode2)

    // Assert
    expect(childComponent.internalSelectedResults).toEqual([ apiJobCode1 ])
  })

  it("joinNameAndCode should return", () => {
    // Arrange
    // noinspection MagicNumberJS
    const apiJobCode = new ApiJobCode(createMockJobcode(42, "foo", "bar"))

    // Act
    const result = childComponent.joinNameAndCode(apiJobCode)

    // Assert
    expect(result).toEqual("foo  bar")
  })

  it("currentSelection should be emitted", () => {
    // Arrange
    hostSelectedCodes = undefined
    const apiJobCode1 = new ApiJobCode(createMockJobcode(1, "job 1", "job1"))
    const apiJobCode2 = new ApiJobCode(createMockJobcode(2, "job 2", "job2"))
    const expected = [ apiJobCode1.code, apiJobCode2.code ]
    childComponent.internalSelectedResults = [ apiJobCode1 ]
    let selectedCodes: string[] = []
    childComponent.currentSelection.pipe(first()).subscribe(
      // tslint:disable-next-line:variable-name
      (codes) => { selectedCodes = codes; return }
    )

    // Act
    console.log("FormFieldSearchSelectJobCodeComponent.spec: 1", childComponent.internalSelectedResults.map(j => j.code))
    childComponent.selectResult(apiJobCode2)
    console.log("FormFieldSearchSelectJobCodeComponent.spec: 2", childComponent.internalSelectedResults.map(j => j.code))

    // Assert
    while (!hostSelectedCodes) {}  // wait for response
    expect(selectedCodes).toEqual(expected)
    expect(hostSelectedCodes).toEqual(expected)
  })
})
