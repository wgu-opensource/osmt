import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SkillImportComponent } from './skill-import.component';
import {
  AuthServiceStub,
  CollectionServiceStub,
} from "@test/resource/mock-stubs";
import {ActivatedRoute} from "@angular/router";
import {CollectionService} from "../../collection/service/collection.service";
import {ActivatedRouteStubSpec} from "@test/util/activated-route-stub.spec";
import {AuthService} from "../../auth/auth-service";

describe('SkillImportComponent', () => {
  let component: SkillImportComponent;
  let fixture: ComponentFixture<SkillImportComponent>;
  let activatedRoute: ActivatedRouteStubSpec

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SkillImportComponent],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub }
      ]
    });
    fixture = TestBed.createComponent(SkillImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
