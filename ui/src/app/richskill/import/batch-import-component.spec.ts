import { HttpClientModule } from '@angular/common/http';
import { Type } from '@angular/core';
import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRouteStubSpec } from 'test/util/activated-route-stub.spec';
import { AuthServiceStub } from '../../../../test/resource/mock-stubs';
import { AppConfig } from '../../app.config';
import { AuthService } from '../../auth/auth-service';
import { EnvironmentService } from '../../core/environment.service';
import { BatchImportComponent, ImportStep } from './batch-import.component';
import { getBaseApi } from '../../api-versions';

export function createComponent(T: Type<BatchImportComponent>): Promise<void> {
  fixture = TestBed.createComponent(T);
  component = fixture.componentInstance;

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges();

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges();
  });
}

let activatedRoute: ActivatedRouteStubSpec;
let component: BatchImportComponent;
let fixture: ComponentFixture<BatchImportComponent>;

describe('BatchImportComponent', () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec();
  });

  beforeEach(waitForAsync(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy();

    TestBed.configureTestingModule({
      declarations: [BatchImportComponent],
      imports: [
        // FormsModule,  // Required for ([ngModel])
        RouterTestingModule, // Required for routerLink
        HttpClientModule,
      ],
      providers: [
        AppConfig,
        EnvironmentService,
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useClass: AuthServiceStub },
        {
          provide: 'BASE_API',
          useFactory: getBaseApi,
        },
      ],
    }).compileComponents();

    const appConfig = TestBed.inject(AppConfig);
    AppConfig.settings = appConfig.defaultConfig();

    createComponent(BatchImportComponent);
  }));

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('stepName should return proper step string', () => {
    expect(component.stepName(0 as any)).toEqual('');
    expect(component.stepName(ImportStep.UploadFile)).toEqual('Select File');
    expect(component.stepName(ImportStep.FieldMapping)).toEqual('Map Fields');
    expect(component.stepName(ImportStep.ReviewRecords)).toEqual(
      'Review and Import'
    );
    expect(component.stepName(ImportStep.Success)).toEqual('Success!');
  });

  it('nextButtonLabel should return Next/Import correctly', () => {
    expect(component.nextButtonLabel).toEqual('Next');
    component.currentStep = ImportStep.ReviewRecords;
    expect(component.nextButtonLabel).toEqual('Import');
  });

  it('cancelButtonLabel should return Cancel Import/Cancel correctly', () => {
    expect(component.cancelButtonLabel).toEqual('Cancel');
    component.currentStep = ImportStep.FieldMapping;
    expect(component.cancelButtonLabel).toEqual('Cancel Import');
  });

  it('recordCount should return correct count', () => {
    expect(component.recordCount).toEqual(0);
  });

  it('validCount should return correct count', () => {
    expect(component.validCount).toEqual(0);
  });

  it('handleClickNext should return false', () => {
    expect(component.handleClickNext()).toBeFalse();
    component.currentStep = 3;
    expect(component.handleClickNext()).toBeFalse();
  });

  it('handleClickCancel should set current step to previous value', () => {
    expect(component.handleClickCancel()).toBeFalse();
    component.currentStep = ImportStep.FieldMapping;
    component.handleClickCancel();
    expect(component.currentStep).toBe(ImportStep.UploadFile as any);
  });
});
