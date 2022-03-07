// noinspection LocalVariableNamingConventionJS

import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { first } from "rxjs/operators"
import { createMockSkillSummary } from "../../../../test/resource/mock-data"
import { PublishStatus } from "../../PublishStatus"
import { TableActionDefinition } from "../../table/skills-library-table/has-action-definitions"
import { ApiSkillSummary } from "../ApiSkillSummary"
import { SkillListRowComponent } from "./skill-list-row.component"


const EXPECTED_SKILL: ApiSkillSummary | null = null  // = new ApiSkillSummary(createMockSkillSummary())
const EXPECTED_SELECTED = true
const EXPECTED_ID = "myId"
const EXPECTED_NEXTID = "myId-name"
const EXPECTED_ROWACTIONS: TableActionDefinition[] = []

// This is atypical because the selector is in brackets: [app-skill-list-row]
@Component({
  template: `
    <tr
      app-skill-list-row
      [skill]="mySkill"
      [isSelected]="myIsSelected"
      [id]="myId"
      [nextId]="myNextId"
      [rowActions]="myRowActions"
      (rowSelected)="onRowSelected($event)"
      (focusActionBar)="onFocusActionBar($event)"    >
    </tr>`
})
class TestHostComponent {
  myId = EXPECTED_ID
  mySkill = EXPECTED_SKILL
  myIsSelected = EXPECTED_SELECTED
  myNextId = EXPECTED_NEXTID
  myRowActions = EXPECTED_ROWACTIONS

  onRowSelected($event: ApiSkillSummary): void {
    console.log("TestHostComponent.onRowSelected: called: event=", $event)
  }

  onFocusActionBar($event: void): void {
    console.log("TestHostComponent.onFocusActionBar: called: event=", $event)
  }
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(SkillListRowComponent))
  childComponent = debugEl.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  hostFixture.detectChanges()

  return hostFixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    hostFixture.detectChanges()
  })
}


let hostFixture: ComponentFixture<TestHostComponent>
let hostComponent: TestHostComponent
let childComponent: SkillListRowComponent


describe("SkillListRowComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        SkillListRowComponent,
        TestHostComponent
      ]
    })
    .compileComponents()

    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(hostComponent).toBeTruthy()
    expect(childComponent).toBeTruthy()
  })

  it("should assign id correctly", () => {
    expect(childComponent.id).toEqual(EXPECTED_ID)
  })
  it("should change input dynamically", () => {
    // Arrange

    // Act
    const newValue = "new ID"
    hostComponent.myId = newValue
    return hostFixture.whenStable().then(() => {
      // 2nd change detection displays the async-fetched hero
      hostFixture.detectChanges()

      // Assert
      expect(childComponent.id).toEqual(newValue)
    })
  })

  it("getFormattedKeywords should return", () => {
    // Arrange
    const skillSummary = createMockSkillSummary()
    const expected = skillSummary.keywords.join("; ")
    hostComponent.mySkill = new ApiSkillSummary(skillSummary)

    return hostFixture.whenStable().then(() => {
      hostFixture.detectChanges()

      // Act
      const result = childComponent.getFormattedKeywords()

      // Assert
      expect(result).toEqual(expected)
    })
  })

  it("getFormattedOccupations should return", () => {
    // Arrange
    const skillSummary = createMockSkillSummary()
    const expected = skillSummary.occupations.map(o => o.detailed).join("; ")
    hostComponent.mySkill = new ApiSkillSummary(skillSummary)

    return hostFixture.whenStable().then(() => {
      hostFixture.detectChanges()

      // Act
      const result = childComponent.getFormattedOccupations()

      // Assert
      expect(result).toEqual(expected)
    })
  })

  it("selected should emit", () => {
    // Arrange
    let skillSummary: ApiSkillSummary
    // noinspection AssignmentResultUsedJS
    childComponent.rowSelected.pipe(first()).subscribe((skill: ApiSkillSummary) => skillSummary = skill)
    const expected = new ApiSkillSummary(createMockSkillSummary())
    hostComponent.mySkill = expected

    return hostFixture.whenStable().then(() => {
      hostFixture.detectChanges()

      // Act
      childComponent.selected()

      // Assert
      expect(skillSummary).toEqual(expected)
    })
  })
  it("selected should not emit", () => {
    // Arrange
    hostFixture.detectChanges()

    // Act
    try {
      childComponent.selected()
    } catch (e) {
      expect(e instanceof Error).toBeTrue()
      return
    }

    // Assert
    expect(false).toBeTrue()
  })

  it("isStatus should return status", () => {
    // Arrange
    childComponent.skill = new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Draft))

    // Act
    const result = childComponent.isStatus(PublishStatus.Draft)

    // Assert
    expect(result).toBeTrue()

    // Act 2
    const result2 = childComponent.isStatus(PublishStatus.Published)

    // Assert 2
    expect(result2).toBeFalse()
  })
  it("isStatus should return false", () => {
    // Arrange

    // Act
    const result = childComponent.isStatus(PublishStatus.Draft)

    // Assert
    expect(result).toBeFalse()
  })

  it("isPublished should return", () => {
    // Arrange
    childComponent.skill = new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Published))

    // Act
    const result = childComponent.isPublished()

    // Assert
    expect(result).toBeTrue()
  })

  it("isArchived should return", () => {
    // Arrange
    childComponent.skill = new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Archived))

    // Act
    const result = childComponent.isArchived()

    // Assert
    expect(result).toBeTrue()
  })

  it("handleClickNext should return", () => {
    // Arrange
    childComponent.skill = new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Archived))

    return hostFixture.whenStable().then(() => {
      hostFixture.detectChanges()

      // Act
      const result = childComponent.handleClickNext()

      // Assert
      expect(result).toBeFalse()
    })
  })
  it("handleClickNext with no nextId should return", () => {
    // Arrange
    childComponent.skill = new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Archived))
    childComponent.nextId = ""

    return hostFixture.whenStable().then(() => {
      hostFixture.detectChanges()

      // Act
      const result = childComponent.handleClickNext()

      // Assert
      expect(result).toBeFalse()
    })
  })
  it("handleClickNext with no target should return", () => {
    // Arrange
    childComponent.skill = new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Archived))
    childComponent.nextId = "no existing element id"

    return hostFixture.whenStable().then(() => {
      hostFixture.detectChanges()

      // Act
      const result = childComponent.handleClickNext()

      // Assert
      expect(result).toBeFalse()
    })
  })
})
