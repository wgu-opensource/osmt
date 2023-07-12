import { Location } from "@angular/common"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { Papa, ParseResult } from "ngx-papaparse"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { TestPage } from "test/util/test-page.spec"
import {CollectionServiceStub, EnvironmentServiceStub, RichSkillServiceStub} from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { EnvironmentService } from "../../core/environment.service"
import { ToastService } from "../../toast/toast.service"
import { RichSkillService } from "../service/rich-skill.service"
import { BatchImportComponent, ImportStep } from "./batch-import.component"
import { CollectionService } from "../../collection/service/collection.service";
import { BatchImportOptionsEnum } from "./BatchImportOptionsEnum";


class Page extends TestPage<BatchImportComponent> {
  get myElement(): HTMLInputElement { return this.query<HTMLInputElement>("#mycomponent-mytype-myelement") }

//  myMethodSpy: jasmine.Spy

  constructor(aFixture: ComponentFixture<BatchImportComponent>) {
    super(aFixture)

    const aComponent = aFixture.componentInstance
    // this.myMethodSpy = spyOn(aComponent, 'myMethodSpy').and.callThrough();
  }
}


export function createComponent(T: Type<BatchImportComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  page = new Page(fixture)

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}

let activatedRoute: ActivatedRouteStubSpec
let component: BatchImportComponent
let fixture: ComponentFixture<BatchImportComponent>
let page: Page

describe("BatchImportComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        BatchImportComponent
      ],
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        Location,
        ToastService,
        Papa,
        Title,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub }
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    // const environmentService = TestBed.inject(EnvironmentService)

    activatedRoute.setParams({ userId: 126 })
    createComponent(BatchImportComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("stepName should be correct", () => {
    expect(Object.keys(ImportStep).length / 2).toEqual(4)  // /2 because enums have a reverse mapping too: { "Foo": 1, "1": "Foo" }
    expect(component.stepName(ImportStep.UploadFile)).toEqual("Select File")
    expect(component.stepName(ImportStep.FieldMapping)).toEqual("Map Fields")
    expect(component.stepName(ImportStep.ReviewRecords)).toEqual("Review and Import")
    expect(component.stepName(ImportStep.Success)).toEqual("Success!")
  })

  it("nextButtonLabel should be correct", () => {
    // Arrange
    component.currentStep = ImportStep.ReviewRecords

    // Act
    const result = component.nextButtonLabel

    // Assert
    expect(result).toEqual("Import")
  })

  it("cancelButtonLabel should be correct", () => {
    // Arrange
    component.currentStep = ImportStep.FieldMapping

    // Act
    const result = component.cancelButtonLabel

    // Assert
    expect(result).toEqual("Cancel Import")
  })

  it("recordCount should be correct", () => {
    // Arrange
    component.parseResults = makeResults()

    // Act
    const count = component.recordCount

    // Assert
    expect(count).toEqual(component.parseResults.data.length)
  })

  it("hideStepLoader should be correct", () => {
    component.hideStepLoader()
    expect(component.stepLoaded).toBeFalsy()
  })

  it("showStepLoader should be correct", () => {
    component.showStepLoader()
    expect(component.stepLoaded).toBeTruthy()
  })

  it("handleClickNext should handle", () => {
    [
      { input: ImportStep.UploadFile, expected: ImportStep.FieldMapping },
      { input: ImportStep.FieldMapping, expected: ImportStep.ReviewRecords },
      { input: ImportStep.ReviewRecords, expected: ImportStep.Success },
    ].forEach((params) => {
      // Arrange
      component.parseResults = makeResults()
      component.currentStep = params.input

      // Act
      component.handleClickNext()

      // Assert
      expect(component.currentStep.valueOf()).toEqual(params.expected)
    })
  })

  it("handleClickCancel should handle", () => {
    [
      { input: ImportStep.ReviewRecords, expected: ImportStep.FieldMapping },
      { input: ImportStep.FieldMapping, expected: ImportStep.UploadFile },
      { input: ImportStep.UploadFile, expected: ImportStep.UploadFile },
      { input: ImportStep.Success, expected: ImportStep.Success },
    ].forEach((params) => {
      // Arrange
      component.parseResults = makeResults()
      component.currentStep = params.input

      // Act
      component.handleClickCancel()

      // Assert
      expect(component.currentStep.valueOf()).toEqual(params.expected)
    })
  })

  it("isMappingValid should return true", () => {
    // Arrange
    component.fieldMappings = {
      key1: "skillName",
      key2: "skillStatement"
    }

    // Act
    const result = component.isMappingValid()

    // Assert
    expect(result).toBeTruthy()
  })

  it("isMappingValid should detect empty map", () => {
    // Arrange
    component.fieldMappings = undefined

    // Act
    const result = component.isMappingValid()

    // Assert
    expect(result).toBeFalse()
  })

  it("isMappingValid should detect missing key", () => {
    // Arrange
    component.fieldMappings = {
      // key1: "skillName",
      key2: "skillStatement"
    }

    // Act
    const result = component.isMappingValid()

    // Assert
    expect(result).toBeFalse()
  })

  it("isMappingValid should detect duplicate", () => {
    // Arrange
    component.fieldMappings = {
      key1: "skillName",
      key2: "skillStatement",
      key3: "skillStatement"
    }

    // Act
    const result = component.isMappingValid()

    // Assert
    expect(result).toBeFalse()
  })

  it("handleFileDrop should be correct", () => {
    // Arrange

    // Act
    const result = component.handleFileDrop(new DragEvent("x"))

    // Assert
    expect(result).toBeFalse()
  })

  it("handleFileDrag should be correct", () => {
    // Arrange

    // Act
    const result = component.handleFileDrag(new DragEvent("x"))

    // Assert
    expect(result).toBeFalse()
  })

  it("handleFileLeave should be correct", () => {
    // Arrange

    // Act
    const result = component.handleFileLeave(new DragEvent("x"))

    // Assert
    expect(result).toBeTruthy()
  })

  it("handleFileChange should be correct", () => {
    // Arrange
    component.currentStep = ImportStep.Success
    const f = new File([""], "filename", { type: "text/html" })
    const evt = { target: { files: [f] }}

    // Act
    const result = component.handleFileChange(evt as unknown as Event)

    // Assert
    expect(component.currentStep.valueOf()).toEqual(ImportStep.UploadFile)
  })

  it("duplicateFieldNames should detect single duplicate", () => {
    // Arrange
    component.fieldMappings = {
      key1: "skillName",
      key2: "skillStatement",
      key3: "skillStatement"
    }

    // Act
    const result = component.duplicateFieldNames()

    // Assert
    expect(result).toEqual("\"Skill Statement\"")
  })

  it("duplicateFieldNames should detect duplicates", () => {
    // Arrange
    component.fieldMappings = {
      key1: "skillName",
      key2: "skillName",
      key3: "skillStatement",
      key4: "skillStatement"
    }

    // Act
    const result = component.duplicateFieldNames()
    // Assert
    expect(result).toEqual("\"RSD Name\", and \"Skill Statement\"")
  })

  it("handleSimilarityOk should set correctly", () => {
    // Arrange/Act
    component.handleSimilarityOk(true)
    // Assert
    expect(component.importSimilarSkills).toBeTrue()

    // Arrange/Act
    component.handleSimilarityOk(false)
    // Assert
    expect(component.importSimilarSkills).toBeFalse()
  })

  it("getImportOptionButtonLabel() should return correct values", () => {
    component.to = BatchImportOptionsEnum.new
    expect(component.getImportOptionButtonLabel()).toEqual("Add to a new Collection")
    component.to = BatchImportOptionsEnum.workspace
    expect(component.getImportOptionButtonLabel()).toEqual("Add to Workspace")
    component.to = BatchImportOptionsEnum.existing
    expect(component.getImportOptionButtonLabel()).toEqual("Add to existing Collection")
  })
})


function makeResults(): ParseResult {
  return {
        data: [
            {
                "RSD Name": "",
                Authors: "Data and Data Store Access",
                "Skill Statement": ".NET Framework",
                Categories: "Access data and data stores using the .NET Framework.",
                Keywords: ".NET Framework; ADO.NET; Language Integrated Query (LINQ); WCF Data Services; XML",
                Standards: "15-0000",
                Certifications: "15-1200",
                "Occupation Major Groups": "15-1220; 15-1250; 15-1290",
                "Occupation Minor Groups": "15-1251; 15-1252; 15-1253; 15-1254; 15-1255; 15-1221; 15-1299",
                "Broad Occupations": "",
                "Detailed Occupations": "",
                "O*NET Job Codes": "",
                Employers: ".NET Framework",
                "Alignment Name": "https://skills.emsidata.com/skills/KS1200B62W5ZF38RJ7TD"
            },
            {
                "RSD Name": ""
            }
        ],
        errors: [
            {
                type: "FieldMismatch",
                code: "TooFewFields",
                message: "Too few fields: expected 15 fields but parsed 14",
                row: 0
            },
            {
                type: "FieldMismatch",
                code: "TooFewFields",
                message: "Too few fields: expected 15 fields but parsed 1",
                row: 1
            }
        ],
        meta: {
            delimiter: ",",
            linebreak: "\n",
            aborted: false,
            truncated: false,
            // cursor: 604,
            fields: [
                "RSD Name",
                "Authors",
                "Skill Statement",
                "Categories",
                "Keywords",
                "Standards",
                "Certifications",
                "Occupation Major Groups",
                "Occupation Minor Groups",
                "Broad Occupations",
                "Detailed Occupations",
                "O*NET Job Codes",
                "Employers",
                "Alignment Name",
                "Alignment URL"
            ]
        }
    }
  }
