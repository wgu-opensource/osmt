import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { RouterTestingModule } from "@angular/router/testing"
import { first } from "rxjs/operators"
import { TestPage } from "../../../../../test/util/test-page.spec"
import { AbstractAdvancedSearchActionBarComponent } from "./abstract-advanced-search-action-bar.component"


// An example of how to test an @Output field


@Component({
  template: `
    <button id="skillButton" (click)="skillButtonClicked()"></button>
    <button id="collectionButton" (click)="collectionButtonClicked()"></button>`
})
class ConcreteComponent extends AbstractAdvancedSearchActionBarComponent {
  constructor() {
    super()
  }
}


class Page extends TestPage<ConcreteComponent> {
  get skillButton(): HTMLButtonElement { return this.query<HTMLButtonElement>("#skillButton") }
  get collectionButton(): HTMLButtonElement { return this.query<HTMLButtonElement>("#collectionButton") }
}


export function createComponent(T: Type<ConcreteComponent>): Promise<void> {
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


let component: ConcreteComponent
let fixture: ComponentFixture<ConcreteComponent>
let page: Page


describe("AbstractAdvancedSearchActionBarComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConcreteComponent
      ],
      imports: [
        RouterTestingModule
      ]
    })
    .compileComponents()

    createComponent(ConcreteComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should emit search skills clicked", () => {
    // Arrange
    let clicked = false
    component.searchSkillsClicked.pipe(first()).subscribe(
      () => { clicked = true; return }
    )

    // Act
    page.skillButton.click()

    // Assert
    expect(clicked).toBeTruthy()
  })

  it("should emit search skills clicked", () => {
    // Arrange
    let clicked = false
    component.searchCollectionsClicked.pipe(first()).subscribe(
      () => { clicked = true; return }
    )

    // Act
    page.collectionButton.click()

    // Assert
    expect(clicked).toBeTruthy()
  })
})
