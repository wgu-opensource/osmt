import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ActivatedRoute, Router } from "@angular/router"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { RichSkillServiceStub } from "../../../../../test/resource/mock-stubs"
import { RichSkillService } from "../../../richskill/service/rich-skill.service"
import { ToastService } from "../../../toast/toast.service"
import { PublicCollectionDetailCardComponent } from "./public-collection-detail-card.component"
import {CollectionPipe} from "../../../pipes"


export function createComponent(T: Type<PublicCollectionDetailCardComponent>): Promise<void> {
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
let component: PublicCollectionDetailCardComponent
let fixture: ComponentFixture<PublicCollectionDetailCardComponent>


describe("PublicCollectionDetailCardComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        PublicCollectionDetailCardComponent,
        CollectionPipe
      ],
      imports: [
      ],
      providers: [
        ToastService,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    createComponent(PublicCollectionDetailCardComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })
})
