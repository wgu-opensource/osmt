import { TestBed } from "@angular/core/testing"
import { ExternalSearchCollectionTableComponent } from "./external-search-collection-table.component"
import { createComponent } from "../../../../../../test/util/test-util.spec"

describe("ExternalSearchCollectionTableComponent", () => {
  let component: ExternalSearchCollectionTableComponent

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ExternalSearchCollectionTableComponent
      ],
      imports: [],
      providers: []
    })
    .compileComponents()

    component = await createComponent(ExternalSearchCollectionTableComponent)
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })
})
