import { TestBed } from "@angular/core/testing"
import { ExternalSearchCollectionRowComponent } from "./external-search-collection-row.component"
import { ApiCollectionSearchResult } from "../../api/ApiCollectionSearchResult"
import { CollectionService } from "../../../../collection/service/collection.service"
import { CollectionServiceStub } from "../../../../../../test/resource/mock-stubs"
import { ToastService } from "../../../../toast/toast.service"
import { createComponent } from "../../../../../../test/util/test-util.spec"
import { createMockApiCollectionSearchResult } from "../../../../../../test/resource/mock-data"

describe("ExternalSearchCollectionRowComponent", () => {
  let component: ExternalSearchCollectionRowComponent
  let searchResult: ApiCollectionSearchResult

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ExternalSearchCollectionRowComponent
      ],
      imports: [],
      providers: [
        ToastService,
        { provide: CollectionService, useClass: CollectionServiceStub }
      ]
    })
    .compileComponents()

    searchResult = createMockApiCollectionSearchResult()

    component = await createComponent(
      ExternalSearchCollectionRowComponent,
      (c) => {
        c.searchResult = searchResult
        c.id = "item1"
        c.nextId =  "item2"
      }
    )
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should import collection", () => {
    spyOn(window, "confirm").and.returnValue(true)
    expect(() => component.onImportCollectionClicked()).not.toThrow()
  })
})
