import { Location } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRouteStubSpec } from 'test/util/activated-route-stub.spec';
import {
  CollectionServiceStub,
  EnvironmentServiceStub,
} from '../../../../test/resource/mock-stubs';
import { AppConfig } from '../../app.config';
import { EnvironmentService } from '../../core/environment.service';
import { ToastService } from '../../toast/toast.service';
import { CollectionService } from '../service/collection.service';
import { CollectionFormComponent } from './collection-form.component';

describe('CollectionFormComponent duplicate mode', () => {
  let component: CollectionFormComponent;
  let fixture: ComponentFixture<CollectionFormComponent>;
  let activatedRoute: ActivatedRouteStubSpec;

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec();
  });

  beforeEach(waitForAsync(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    Object.defineProperty(routerSpy, 'url', {
      configurable: true,
      get: () => '/collections/uuid1/duplicate',
    });

    TestBed.configureTestingModule({
      declarations: [CollectionFormComponent],
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [
        AppConfig,
        Location,
        Title,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    const appConfig = TestBed.inject(AppConfig);
    AppConfig.settings = appConfig.defaultConfig();

    const environmentService = TestBed.inject(EnvironmentService);
    environmentService.environment.editableAuthor = true;
    AppConfig.settings.editableAuthor = true;

    activatedRoute.setParams({ uuid: 'uuid1' });
    fixture = TestBed.createComponent(CollectionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    return fixture.whenStable().then(() => {
      fixture.detectChanges();
    });
  }));

  it('should set isDuplicating from the router url', () => {
    expect(component.isDuplicating).toBeTrue();
  });

  it('should prefill name with Copy suffix including timestamp', () => {
    const nameValue = component.collectionForm.get('collectionName')?.value;
    expect(nameValue).toMatch(
      /my collection name \(Copy \d{4}-\d{2}-\d{2} \d{2}:\d{2}\)/
    );
  });

  it('collectionNameErrorMessage should describe not-a-copy validation', () => {
    component.collectionForm.patchValue({
      collectionName: 'Collection (Copy 2024-01-01 10:00)',
    });
    expect(component.collectionNameErrorMessage).toContain('(Copy ...)');
  });
});
