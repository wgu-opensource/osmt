import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import { ActivatedRoute, Router } from "@angular/router";
import { ActivatedRouteStubSpec } from "@test/util/activated-route-stub.spec";
import { Type } from "@angular/core";
import { RouterTestingModule } from "@angular/router/testing";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { Location } from "@angular/common";
import { Title } from "@angular/platform-browser";
import { BatchImportCollectionComponent } from './batch-import-collection.component';
import { AppConfig } from "../../../app.config";
import { ToastService } from "../../../toast/toast.service";
import { EnvironmentService } from "../../../core/environment.service";
import { CollectionServiceStub, EnvironmentServiceStub, RouterStub } from "@test/resource/mock-stubs";
import { CollectionService } from "../../service/collection.service";


let activatedRoute: ActivatedRouteStubSpec
let component: BatchImportCollectionComponent
let fixture: ComponentFixture<BatchImportCollectionComponent>

export function createComponent(T: Type<BatchImportCollectionComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}

describe('BatchImportCollectionComponent', () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  let fixture: ComponentFixture<BatchImportCollectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BatchImportCollectionComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        Location,
        Title,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useClass: RouterStub },
      ]
    })
      .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    const environmentService = TestBed.inject(EnvironmentService)
    environmentService.environment.editableAuthor = true
    AppConfig.settings.editableAuthor = true  // Doubly sure

    activatedRoute.setParams({ uuid: "uuid1" })
    createComponent(BatchImportCollectionComponent)
  }));

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("nameLabel should return", () => {
    component.collectionUuid = ""
    expect(component.nameLabel).toEqual("New Collection Name")

    component.collectionUuid = "uuid"
    expect(component.nameLabel).toEqual("Collection Name")
  })
});
