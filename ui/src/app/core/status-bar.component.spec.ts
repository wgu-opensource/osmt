import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { By } from "@angular/platform-browser"
import { PublishStatus } from "../PublishStatus"
import { StatusBarComponent } from "./status-bar.component"


const EXPECTED_STATUS = PublishStatus.Deleted
const EXPECTED_PUBLISH_DATE = "2020-05-25T14:58:46.313Z"
const EXPECTED_ARCHIVE_DATE = "2020-06-25T14:58:46.313Z"
const EXPECTED_SHOW_DATES = false

@Component({
  template: `
    <app-status-bar
      [status]="myStatus"
      [publishDate]="myPublishDate"
      [archiveDate]="myArchiveDate"
      [showDates]="myShowDates"
    >
    </app-status-bar>`
})
class TestHostComponent {
  myStatus = EXPECTED_STATUS
  myPublishDate = EXPECTED_PUBLISH_DATE
  myArchiveDate = EXPECTED_ARCHIVE_DATE
  myShowDates = EXPECTED_SHOW_DATES
}


export function createComponent(T: Type<TestHostComponent>): Promise<void> {
  hostFixture = TestBed.createComponent(T)
  hostComponent = hostFixture.componentInstance

  const debugEl = hostFixture.debugElement.query(By.directive(StatusBarComponent))
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
let childComponent: StatusBarComponent


describe("StatusBarComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        StatusBarComponent,
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

  it("should assign inputs", () => {
    expect(childComponent.status).toEqual(EXPECTED_STATUS)
    expect(childComponent.publishDate).toEqual(EXPECTED_PUBLISH_DATE)
    expect(childComponent.archiveDate).toEqual(EXPECTED_ARCHIVE_DATE)
    expect(childComponent.showDates).toEqual(EXPECTED_SHOW_DATES)
  })

  it("should be archived", () => {
    childComponent.archiveDate = "2020-02-25T14:58:46.313Z"
    childComponent.status = PublishStatus.Published
    expect(childComponent.isArchived()).toBeTruthy()

    childComponent.archiveDate = "2020-03-25T14:58:46.313Z"
    childComponent.status = PublishStatus.Deleted
    expect(childComponent.isArchived()).toBeTruthy()

    childComponent.archiveDate = ""
    childComponent.status = PublishStatus.Archived
    expect(childComponent.isArchived()).toBeTruthy()

    childComponent.archiveDate = ""
    childComponent.status = PublishStatus.Archived
    expect(childComponent.isArchived()).toBeTruthy()
  })

  it("should be published", () => {
    childComponent.publishDate = "2020-06-25T14:58:46.313Z"
    childComponent.status = PublishStatus.Published
    expect(childComponent.isPublished()).toBeTruthy()

    childComponent.publishDate = "2020-05-25T14:58:46.313Z"
    childComponent.status = PublishStatus.Unarchived
    expect(childComponent.isPublished()).toBeTruthy()

    childComponent.publishDate = ""
    childComponent.status = PublishStatus.Published
    expect(childComponent.isPublished()).toBeTruthy()

    childComponent.publishDate = ""
    childComponent.status = PublishStatus.Unarchived
    expect(childComponent.isPublished()).toBeFalsy()
  })

  it("should be draft", () => {
    childComponent.publishDate = "2020-06-25T14:58:46.313Z"
    childComponent.status = PublishStatus.Published
    expect(childComponent.isDraft()).toBeFalsy()

    childComponent.publishDate = "2020-05-25T14:58:46.313Z"
    childComponent.status = PublishStatus.Unarchived
    expect(childComponent.isDraft()).toBeFalsy()

    childComponent.publishDate = ""
    childComponent.status = PublishStatus.Published
    expect(childComponent.isDraft()).toBeFalsy()

    childComponent.publishDate = ""
    childComponent.status = PublishStatus.Unarchived
    expect(childComponent.isDraft()).toBeTruthy()
  })
})
