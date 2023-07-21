import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNamedReferenceComponent } from './create-named-reference.component';
import { ActivatedRoute, Router } from "@angular/router"
import { ActivatedRouteStubSpec } from "@test/util/activated-route-stub.spec"
import { BrowserModule } from "@angular/platform-browser"
import { RouterTestingModule } from "@angular/router/testing"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { NamedReferenceService } from "../service/named-reference.service"
import { AuthService } from "../../../auth/auth-service"
import { AuthServiceStub, RouterStub } from "@test/resource/mock-stubs"
import { getBaseApi } from "../../../api-versions"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"
import { AbstractDataService } from "../../../data/abstract-data.service"
import { createMockNamedReference2 } from "@test/resource/mock-data"
import { AppConfig } from "../../../app.config"

describe('CreateNamedReferenceComponent', () => {
  let component: CreateNamedReferenceComponent;
  let fixture: ComponentFixture<CreateNamedReferenceComponent>;
  let activatedRoute: ActivatedRouteStubSpec
  let service: AbstractDataService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CreateNamedReferenceComponent],
      imports: [
        BrowserModule,
        RouterTestingModule,
        HttpClientTestingModule,
        ReactiveFormsModule,
        FormsModule
      ],
      providers: [
        AppConfig,
        NamedReferenceService,
        { provide: AuthService, useClass: AuthServiceStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useClass: RouterStub },
      ]
    });
    fixture = TestBed.createComponent(CreateNamedReferenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = TestBed.inject(NamedReferenceService)

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it("form definitions should be correct", () => {
    const formDefinition = component.getFormDefinitions()
    expect(formDefinition).toBeTruthy()
    expect(formDefinition.get('name')).toBeTruthy()
    expect(formDefinition.get('type')).toBeTruthy()
    expect(formDefinition.get('url')).toBeTruthy()
    expect(formDefinition.get('framework')).toBeTruthy()
  })

  it("submit should create", () => {
    const spyService = spyOn(service, "create")
    component.id = -1
    component.onSubmit()
    expect(spyService).toHaveBeenCalled()
  })

  it("submit should update", () => {
    const spyService = spyOn(service, "update")
    component.id = 1
    component.onSubmit()
    expect(spyService).toHaveBeenCalled()
  })

  it("title should be create", () => {
    component.metadata = undefined
    expect(component.pageTitle()).toContain("Create")
  })

  it("title should be edit", () => {
    component.metadata = createMockNamedReference2()
    expect(component.pageTitle()).toContain("Edit")
  })

  it("on submit should call create", () => {
    const spyService = spyOn(service, "create")
    component.id = -1
    component.onSubmit()
    expect(spyService).toHaveBeenCalled()
  })

  it("on submit should call update", () => {
    const spyService = spyOn(service, "update")
    component.id = 1
    component.onSubmit()
    expect(spyService).toHaveBeenCalled()
  })
});