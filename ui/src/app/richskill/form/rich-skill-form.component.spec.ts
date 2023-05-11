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
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { ToastService } from "../../toast/toast.service"
import {ApiAlignment, ApiNamedReference, ApiSkill, INamedReference} from "../ApiSkill"
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
import {ApiJobCode} from "../../metadata/job-codes/Jobcode";


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
    const result1 = component.nameErrorMessage
    // Assert
    expect(result1).toEqual("Name is still a copy")


    // Arrange
    component.skillForm.get("skillName")?.setValue("")
    // Act
    const result2 = component.nameErrorMessage
    // Assert
    expect(result2).toEqual("Name required")
  })

  it("authorErrorMessage should return error ", () => {
    // Assert
    expect(component.authorErrorMessage).toEqual("Author required")
  })

  it("skillStatementErrorMessage should return error ", () => {
    // Assert
    expect(component.skillStatementErrorMessage).toEqual("Skill Statement required")
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
      createMockNamedReference("id4", "aardvark"),
      createMockNamedReference("id5", "giraffe")
    ]

    const existing = [
      references[0],
      references[1],
      references[2],
      references[3]
    ]

    // Act - adding 1, removing 2
    const updates1 = component.diffNamedReferenceList(
      [references[3], references[4], references[0]],
      existing
    )
    // Assert
    expect(updates1).toEqual(new ApiReferenceListUpdate(
      [ new ApiNamedReference(references[4]) ],
      [ new ApiNamedReference(references[1]), new ApiNamedReference(references[2]) ]
    ))

    // Act - adding 0, removing 2
    const updates2 = component.diffNamedReferenceList(
      [ references[3], references[0] ],
      existing
    )
    // Assert
    expect(updates2).toEqual(new ApiReferenceListUpdate(
      [ ],
      [ new ApiNamedReference(references[1]), new ApiNamedReference(references[2]) ]
    ))

    // Act - adding 1, removing 0
    const updates3 = component.diffNamedReferenceList(
      [references[3], references[1], references[4], references[0], references[2]],
      existing
    )
    // Assert
    expect(updates3).toEqual(new ApiReferenceListUpdate(
      [ new ApiNamedReference(references[4]) ],
      []
    ))

    // Act
    const updates4 = component.diffNamedReferenceList(
      [ references[2], references[3], references[1], references[0] ],
      existing
    )
    // Assert
    expect(updates4).toEqual(undefined)
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
      collections,
      skillName,
      skillStatement,
      // tslint:disable-next-line:no-any
    } = setupForm(false) as any
    component.isDuplicating = false
    component.existingSkill = null  // For this test, assume the best

    const {  // These will be overwritten by the component's selectedXYZ fields
      authors,
      categories,
      certifications,
      employers,
      keywords,
      occupations,
      standards
      // tslint:disable-next-line:no-any
    } = {
      authors: ["author1", "author2"].filter(v => !!v),
      categories: ["category1", "category2"].filter(v => !!v),
      certifications: [ApiNamedReference.fromString("cert1"), ApiNamedReference.fromString("cert2")].filter(v => !!v),
      employers: [ApiNamedReference.fromString("empl1"), ApiNamedReference.fromString("empl2")].filter(v => !!v),
      keywords: ["keyword1", "keyword2"].filter(v => !!v),
      occupations: [new ApiJobCode({ code: "occp1" }), new ApiJobCode({ code: "occp2" })],
      standards: [ApiAlignment.fromString("stnd1"), ApiAlignment.fromString("stnd2")].filter(v => !!v)
    }

    component.skillForm.setValue({
      skillName: skillName,
      skillStatement: skillStatement,
      authors: authors,
      categories: categories,
      collections: collections,
      certifications: certifications,
      employers: employers,
      keywords: keywords,
      occupations: occupations,
      standards: standards
    })

    // Act
    const update = component.updateObject()

    // Assert
    expect(update.skillName).toEqual(skillName)
    expect(update.skillStatement).toEqual(skillStatement)
    expect((update.authors as IStringListUpdate).add).toEqual(authors)
    expect((update.categories as IStringListUpdate).add).toEqual(categories)
    expect((update.keywords as IStringListUpdate).add).toEqual(keywords as string[])
    expect((update.standards as IReferenceListUpdate).add).toEqual(standards as INamedReference[])
    expect((update.collections as IStringListUpdate).add).toEqual(collections)
    expect((update.certifications as IReferenceListUpdate).add).toEqual(certifications as INamedReference[])
    expect((update.occupations as IStringListUpdate).add).toEqual(occupations.map(o => component.stringFromJobCode(o)))
    expect((update.employers as IReferenceListUpdate).add).toEqual(employers as INamedReference[])

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

  it("showAuthors should return", () => {
    expect(component.showAuthors()).toBeTruthy()
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

    activatedRoute.setParams({ uuid: EXPECTED_UUID })
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

  const occupations = [ "occp1", "occp2", "occp3" ]

  const standards = [
    ApiNamedReference.fromString("stnd1"),
    ApiNamedReference.fromString("stnd2"),
    ApiNamedReference.fromString("stnd3")
  ]

  const certifications = [
    ApiNamedReference.fromString("cert1"),
    ApiNamedReference.fromString("cert2"),
    ApiNamedReference.fromString("cert3")
  ]

  const employers = [
    ApiNamedReference.fromString("empl1"),
    ApiNamedReference.fromString("empl2"),
    ApiNamedReference.fromString("empl3")
  ]

  const fields = isBlank
    ? {
      skillName: "",
      skillStatement: "",
      authors: [],
      categories: [],
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
      authors: ["author1", "author2"],
      categories: ["category1", "category2"],
      keywords: ["keyword1", "keyword2", "keyword3"],
      collections: ["collection1", "collection2"],
      occupations: occupations,
      standards: standards,
      certifications: certifications,
      employers: employers
    }
  form.setValue(fields)
  return fields
}
