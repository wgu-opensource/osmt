// noinspection LocalVariableNamingConventionJS

import { Location } from "@angular/common"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { FormBuilder } from "@angular/forms"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { of } from "rxjs"
import {
  createMockNamedReference,
  createMockSkill,
  createMockSkillSummary,
  createMockUuidReference
} from "../../../../test/resource/mock-data"
import { EnvironmentServiceStub, RichSkillServiceStub } from "../../../../test/resource/mock-stubs"
import { ActivatedRouteStubSpec } from "../../../../test/util/activated-route-stub.spec"
import { AppConfig } from "../../app.config"
import { initializeApp } from "../../app.module"
import { EnvironmentService } from "../../core/environment.service"
import { IJobCode } from "../../job-codes/Jobcode"
import { PublishStatus } from "../../PublishStatus"
import { ToastService } from "../../toast/toast.service"
import { ApiNamedReference, ApiSkill, INamedReference } from "../ApiSkill"
import { ApiSkillSummary } from "../ApiSkillSummary"
import {
  ApiReferenceListUpdate,
  ApiSkillUpdate,
  ApiStringListUpdate,
  IReferenceListUpdate,
  IStringListUpdate
} from "../ApiSkillUpdate"
import { RichSkillService } from "../service/rich-skill.service"
import { RichSkillFormComponent } from "./rich-skill-form.component"


export function createComponent(T: Type<RichSkillFormComponent>): Promise<void> {
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
let component: RichSkillFormComponent
let fixture: ComponentFixture<RichSkillFormComponent>


describe("RichSkillFormComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        RichSkillFormComponent
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        EnvironmentService,
        Title,
        FormBuilder,
        Location,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
      ]
    })

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    createComponent(RichSkillFormComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("pageTitle should return title", () => {
    const date = new Date("2020-06-25T14:58:46.313Z")

    // Arrange
    component.isDuplicating = true
    // Act
    const result1 = component.pageTitle()
    // Assert
    expect(result1).toEqual("Edit Copy of RSD")

    // Arrange
    component.isDuplicating = false
    component.existingSkill = new ApiSkill(createMockSkill(date, date, PublishStatus.Draft))
    // Act
    const result2 = component.pageTitle()
    // Assert
    expect(result2).toEqual("Edit Rich Skill Descriptor")

    // Arrange
    component.isDuplicating = false
    component.existingSkill = null
    // Act
    const result3 = component.pageTitle()
    // Assert
    expect(result3).toEqual("Create Rich Skill Descriptor")
  })

  it("nameErrorMessage should return error ", () => {
    // Arrange
    component.skillForm.get("skillName")?.setValue("Copy of unique name")
    // Act
    const result1 = component.nameErrorMessage()
    // Assert
    expect(result1).toEqual("Name is still a copy")


    // Arrange
    component.skillForm.get("skillName")?.setValue("")
    // Act
    const result2 = component.nameErrorMessage()
    // Assert
    expect(result2).toEqual("Name is required")
  })

  it("diffUuidList should be correct", () => {
    // Arrange
    const collections = [
      createMockUuidReference("uuid1", "zebra"),
      createMockUuidReference("uuid2", "hippo"),
      createMockUuidReference("uuid3", "fox"),
      createMockUuidReference("uuid4", "aardvark")
    ]

    // Act - adding 1, removing 2
    const updates1 = component.diffUuidList([ "aardvark", "giraffe", "zebra" ], collections)
    // Assert
    expect(updates1).toEqual(new ApiStringListUpdate(
      [ "giraffe" ],
      [ "hippo", "fox" ]
    ))

    // Act - adding 0, removing 2
    const updates2 = component.diffUuidList([ "aardvark", "zebra" ], collections)
    // Assert
    expect(updates2).toEqual(new ApiStringListUpdate(
      [ ],
      [ "hippo", "fox" ]
    ))

    // Act - adding 1, removing 0
    const updates3 = component.diffUuidList([ "aardvark", "giraffe", "hippo", "zebra", "fox" ], collections)
    // Assert
    expect(updates3).toEqual(new ApiStringListUpdate(
      [ "giraffe" ],
      [ ]
    ))

    // Act
    const updates4 = component.diffUuidList([ "fox", "aardvark", "hippo", "zebra" ], collections)
    // Assert
    expect(updates4).toEqual(undefined)
  })

  it("diffStringList should be correct", () => {
    // Arrange
    const keywords = [
      "zebra",
      "hippo",
      "fox",
      "aardvark"
    ]

    // Act - adding 1, removing 2
    const updates1 = component.diffStringList([ "aardvark", "giraffe", "zebra" ], keywords)
    // Assert
    expect(updates1).toEqual(new ApiStringListUpdate(
      [ "giraffe" ],
      [ "hippo", "fox" ]
    ))

    // Act - adding 0, removing 2
    const updates2 = component.diffStringList([ "aardvark", "zebra" ], keywords)
    // Assert
    expect(updates2).toEqual(new ApiStringListUpdate(
      [ ],
      [ "hippo", "fox" ]
    ))

    // Act - adding 1, removing 0
    const updates3 = component.diffStringList([ "aardvark", "giraffe", "hippo", "zebra", "fox" ], keywords)
    // Assert
    expect(updates3).toEqual(new ApiStringListUpdate(
      [ "giraffe" ],
      [ ]
    ))

    // Act
    const updates4 = component.diffStringList([ "fox", "aardvark", "hippo", "zebra" ], keywords)
    // Assert
    expect(updates4).toEqual(undefined)
  })

  it("diffReferenceList should be correct", () => {
    // Arrange
    const references = [
      createMockNamedReference("id1", "zebra"),
      createMockNamedReference("id2", "hippo"),
      createMockNamedReference("id3", "fox"),
      createMockNamedReference("id4", "aardvark")
    ]

    // Act - adding 1, removing 2
    const updates1 = component.diffReferenceList([ "aardvark", "giraffe", "zebra" ], references)
    // Assert
    expect(updates1).toEqual(new ApiReferenceListUpdate(
      [ new ApiNamedReference({ name: "giraffe" }) ],
      [ new ApiNamedReference({ name: "hippo" }), new ApiNamedReference({ name: "fox" }) ]
    ))

    // Act - adding 0, removing 2
    const updates2 = component.diffReferenceList([ "aardvark", "zebra" ], references)
    // Assert
    expect(updates2).toEqual(new ApiReferenceListUpdate(
      [ ],
      [ new ApiNamedReference({ name: "hippo" }), new ApiNamedReference({ name: "fox" }) ]
    ))

    // Act - adding 1, removing 0
    const updates3 = component.diffReferenceList([ "aardvark", "giraffe", "hippo", "zebra", "fox" ], references)
    // Assert
    expect(updates3).toEqual(new ApiReferenceListUpdate(
      [ new ApiNamedReference({ name: "giraffe" }) ],
      [ ]
    ))

    // Act
    const updates4 = component.diffReferenceList([ "fox", "aardvark", "hippo", "zebra" ], references)
    // Assert
    expect(updates4).toEqual(undefined)
  })

  it("splitTextArea should split into words", () => {
    // Arrange
    const words = "apple; banana;chocolate"   // 'banana' has a space that will be trimmed out
    // Act
    const result = component.splitTextarea(words)
    // Assert
    expect(result).toEqual([ "apple", "banana", "chocolate" ])
  })

  it("nonEmptyOrNull should trim and return null for empty strings", () => {
    expect(component.nonEmptyOrNull(undefined)).toEqual(undefined)
    expect(component.nonEmptyOrNull("")).toEqual(undefined)
    expect(component.nonEmptyOrNull("hello")).toEqual("hello")
    expect(component.nonEmptyOrNull(" padded ")).toEqual("padded")
  })

  it("updateObject should return updates", () => {
    // Arrange
    const {  // These should not be modified
      category,
      collections,
      skillName,
      skillStatement,
      // tslint:disable-next-line:no-any
    } = setupForm(false) as any
    const {  // These will be overwritten by the component's selectedXYZ fields
      certifications,
      employers,
      keywords,
      occupations,
      standards
      // tslint:disable-next-line:no-any
    } = setupSelectedFields(false) as any
    component.isDuplicating = false
    component.existingSkill = null  // For this test, assume the best

    // Act
    const update = component.updateObject()

    // Assert
    expect(update.skillName).toEqual(skillName)
    expect(update.skillStatement).toEqual(skillStatement)
    expect(update.category).toEqual(category)
    expect((update.keywords as IStringListUpdate).add).toEqual(keywords)
    expect((update.standards as IReferenceListUpdate).add).toEqual(standards)
    expect((update.collections as IStringListUpdate).add).toEqual(collections)
    expect((update.certifications as IReferenceListUpdate).add).toEqual(certifications)
    expect((update.occupations as IStringListUpdate).add).toEqual(occupations)
    expect((update.employers as IReferenceListUpdate).add).toEqual(employers)
  })

  it("onSubmit should be correct", () => {
    // Arrange
    const router = TestBed.inject(Router)
    const spyNavigate = spyOn(router, "navigate").and.stub()
    component.isDuplicating = false
    const date = new Date("2020-06-25T14:58:46.313Z")
    const iSkill = createMockSkill(date, date, PublishStatus.Draft)
    const skill = new ApiSkill(iSkill)
    component.existingSkill = skill
    component.skillUuid = iSkill.uuid
    setupForm(false)
    setupSelectedFields(false)
    const richSkillService = TestBed.inject(RichSkillService)
    spyOn(richSkillService, "createSkill").and.callThrough()
    spyOn(richSkillService, "updateSkill").and.callFake(
      (uuid: string, updateObject: ApiSkillUpdate) => of(skill))

    // Act
    component.onSubmit()

    // Assert
    component.skillSaved?.subscribe((skil) => {
      expect(skil).toEqual(new ApiSkill(iSkill))
    })
    expect(richSkillService.createSkill).not.toHaveBeenCalled()
    expect(richSkillService.updateSkill).toHaveBeenCalled()
    expect(router.navigate).toHaveBeenCalledWith(["/skills/my skill uuid/manage"])
  })

  it("namedReferenceString should return NamedReference or undefined", () => {
    expect(component.namedReferenceForString("")).toEqual(undefined)
    expect(component.namedReferenceForString("a://b")).toEqual(new ApiNamedReference({ id: "a://b" }))
    expect(component.namedReferenceForString("abc")).toEqual(new ApiNamedReference({ name: "abc" }))
  })

  it("stringFromJobCode should return string", () => {
    expect(component.stringFromJobCode(undefined)).toEqual("")
    expect(component.stringFromJobCode({ code: "abcd" })).toEqual("abcd")
    expect(component.stringFromJobCode({ name: "id1" } as IJobCode)).toEqual("")
  })

  it("setSkill should be correct", () => {
    // Arrange
    component.isDuplicating = false
    const date = new Date("2020-06-25T14:58:46.313Z")
    const iSkill = createMockSkill(date, date, PublishStatus.Draft)
    const skill = new ApiSkill(iSkill)
    const titleService = TestBed.inject(Title)

    // Act
    component.setSkill(skill)

    // Assert
    expect(component.existingSkill).toEqual(skill)
    expect(titleService.getTitle()).toEqual(`${skill.skillName} | Edit Rich Skill Descriptor | OSMT`)
  })

  it("handleFormErrors should ignore them", () => {
    expect(component.handleFormErrors(undefined)).toBeFalsy()
  })

  it("handleClickCancel should go back", () => {
    // Arrange
    const location = TestBed.inject(Location)
    spyOn(location, "back").and.stub()

    // Act
    const result = component.handleClickCancel()

    // Assert
    expect(result).toBeFalsy()
    expect(location.back).toHaveBeenCalled()
  })

  it("showAuthor should return", () => {
    expect(component.showAuthor()).toBeTruthy()
  })

  it("populateTypeAheadFieldsWithResults should fill form with defaults", () => {
    // Arrange
    const form = component.skillForm.value
    setupSelectedFields(false)

    // Act
    component.populateTypeAheadFieldsWithResults()

    // Assert
    expect(form.standards).toEqual("standard1; standard2")
    expect(form.occupations).toEqual("occupation1; occupation2")
    expect(form.keywords).toEqual("keyword1; keyword2")
    expect(form.certifications).toEqual("certification1; certification2")
    expect(form.employers).toEqual("employer1; employer2")
  })

  it("handleStandardsTypeAheadResults should be correct", () => {
    // Arrange
    component.selectedStandards = []
    const strings = [ "string1", "string2" ]

    // Act
    component.handleStandardsTypeAheadResults(strings)

    // Assert
    expect(component.selectedStandards).toEqual(strings)
  })

  it("handleJobCodesTypeAheadResults should be correct", () => {
    // Arrange
    component.selectedJobCodes = []
    const strings = [ "string1", "string2" ]

    // Act
    component.handleJobCodesTypeAheadResults(strings)

    // Assert
    expect(component.selectedJobCodes).toEqual(strings)
  })

  it("handleKeywordTypeAheadResults should be correct", () => {
    // Arrange
    component.selectedKeywords = []
    const strings = [ "string1", "string2" ]

    // Act
    component.handleKeywordTypeAheadResults(strings)

    // Assert
    expect(component.selectedKeywords).toEqual(strings)
  })

  it("handleCertificationTypeAheadResults should be correct", () => {
    // Arrange
    component.selectedCertifications = []
    const strings = [ "string1", "string2" ]

    // Act
    component.handleCertificationTypeAheadResults(strings)

    // Assert
    expect(component.selectedCertifications).toEqual(strings)
  })

  it("handleEmployersTypeAheadResults should be correct", () => {
    // Arrange
    component.selectedEmployers = []
    const strings = [ "string1", "string2" ]

    // Act
    component.handleEmployersTypeAheadResults(strings)

    // Assert
    expect(component.selectedEmployers).toEqual(strings)
  })

  it("handleStatementBlur should be correct", () => {
    // Arrange
    const value = "test"
    component.skillForm.controls.skillStatement.setValue(value)
    spyOn(component, "checkForStatementSimilarity").and.callFake((statement) => {
      expect(statement).toEqual(value)
    })

    // Act
    component.handleStatementBlur(new FocusEvent("some type"))
  })

  it("hasStatementWarning should detect similar skills", () => {
    component.similarSkills = [ new ApiSkillSummary(createMockSkillSummary())]
    expect(component.hasStatementWarning).toBeTruthy()

    component.similarSkills = []
    expect(component.hasStatementWarning).toBeFalsy()
  })
})

describe("RichSkillFormComponent (with parameter)", () => {
  const EXPECTED_UUID = "126"

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        RichSkillFormComponent
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        EnvironmentService,
        Title,
        FormBuilder,
        Location,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
      ]
    })

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    activatedRoute.setParamMap({ uuid: EXPECTED_UUID })
    createComponent(RichSkillFormComponent)
  }))

  it("should be created with uuid parameter", () => {
    // Arrange
    const richSkillService = TestBed.inject(RichSkillService)
    richSkillService.getSkillByUUID("any").subscribe((skill) => {
      // Assert
      expect(component).toBeTruthy()
      expect(component.skillUuid).toEqual(EXPECTED_UUID)
      expect(component.existingSkill).toEqual(skill)
    })
  })
})


function setupForm(isBlank: boolean): object {
  const form = component.skillForm
  const fields = isBlank
    ? {
      skillName: "",
      skillStatement: "",
      author: "",
      category: "",
      keywords: [],
      collections: [],
      occupations: [],
      standards: [],
      certifications: [],
      employers: []
    }
    : {
      skillName: "my skill",
      skillStatement: "my statement",
      author: "my author",
      category: "my category",
      keywords: ["keyword1", "keyword2", "keyword3"],
      collections: ["collection1", "collection2"],
      occupations: ["occupation1", "occupation2", "occupation3"],
      standards: ["standard1", "standard2", "standard3"],
      certifications: ["certification1", "certification2", "certification3"],
      employers: ["employer1", "employer2", "employer3"]
    }
  form.setValue(fields)
  return fields
}

function setupSelectedFields(isBlank: boolean): object {
  if (isBlank) {
    component.selectedKeywords = [""]
    component.selectedJobCodes = []
    component.selectedStandards = []
    component.selectedCertifications = []
    component.selectedEmployers = []
  }
  else {
    component.selectedKeywords = ["keyword1", "keyword2"]
    component.selectedJobCodes = ["occupation1", "occupation2"]
    component.selectedStandards = ["standard1", "standard2"]
    component.selectedCertifications = ["certification1", "certification2"]
    component.selectedEmployers = ["employer1", "employer2"]
  }

  // Return the new values for easy deconstruction
  return {
    keywords: component.selectedKeywords,
    occupations: component.selectedJobCodes,
    standards: component.selectedStandards.map(x => new ApiNamedReference({ id: undefined, name: x }) as INamedReference),
    certifications: component.selectedCertifications.map(x => new ApiNamedReference({ id: undefined, name: x })),
    employers: component.selectedEmployers.map(x => new ApiNamedReference({ id: undefined, name: x }))
  }
}
