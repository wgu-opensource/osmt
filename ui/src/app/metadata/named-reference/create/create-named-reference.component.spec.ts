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

describe('CreateComponent', () => {
  let component: CreateNamedReferenceComponent;
  let fixture: ComponentFixture<CreateNamedReferenceComponent>;
  let activatedRoute: ActivatedRouteStubSpec

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CreateNamedReferenceComponent],
      imports: [
        BrowserModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
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
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
