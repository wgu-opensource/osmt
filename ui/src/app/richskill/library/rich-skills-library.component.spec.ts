// noinspection MagicNumberJS,LocalVariableNamingConventionJS

import { Component, Type } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import {
  AuthServiceStub,
  CollectionServiceStub,
  RichSkillServiceStub,
} from '../../../../test/resource/mock-stubs';
import { PublishStatus } from '../../PublishStatus';
import { ToastService } from '../../toast/toast.service';
import { RichSkillService } from '../service/rich-skill.service';
import { RichSkillsLibraryComponent } from './rich-skills-library.component';
import { AuthService } from '../../auth/auth-service';
import { HttpClientModule } from '@angular/common/http';
import { CollectionService } from '../../collection/service/collection.service';
import { Title } from '@angular/platform-browser';
import { AppConfig } from '../../app.config';

@Component({
  selector: 'app-test-rich-skills-library',
  template: ``,
})
class TestRichSkillsLibraryComponent extends RichSkillsLibraryComponent {
  public get addToWorkspaceVisiblePublic(): boolean {
    return this.addToWorkspaceVisible();
  }

  public get exportSearchVisiblePublic(): boolean {
    return this.exportSearchVisible();
  }
}

describe('RichSkillsLibraryComponent', () => {
  let component: TestRichSkillsLibraryComponent;
  let fixture: ComponentFixture<TestRichSkillsLibraryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [TestRichSkillsLibraryComponent],
      imports: [RouterTestingModule, HttpClientModule],
      providers: [
        ToastService,
        Title,
        AppConfig,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    const appConfig = TestBed.inject(AppConfig);
    AppConfig.settings = appConfig.defaultConfig(); // This avoids the race condition on reading the config's whitelabel.toolName

    fixture = TestBed.createComponent(TestRichSkillsLibraryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should set isPublicView to true when user is not authenticated', () => {
      // Arrange
      const authService = TestBed.inject(AuthService);
      spyOn(authService, 'isAuthenticated').and.returnValue(false);

      // Act
      component.ngOnInit();

      // Assert
      expect(component.isPublicView).toBe(true);
    });

    it('should filter to Published and Archived only when user is not authenticated', () => {
      // Arrange
      const authService = TestBed.inject(AuthService);
      spyOn(authService, 'isAuthenticated').and.returnValue(false);

      // Act
      component.ngOnInit();

      // Assert
      expect(component.selectedFilters.has(PublishStatus.Published)).toBe(true);
      expect(component.selectedFilters.has(PublishStatus.Archived)).toBe(true);
      expect(component.selectedFilters.has(PublishStatus.Draft)).toBe(false);
    });

    it('should keep default filters when user is authenticated', () => {
      // Arrange
      const authService = TestBed.inject(AuthService);
      spyOn(authService, 'isAuthenticated').and.returnValue(true);

      // Act
      component.ngOnInit();

      // Assert
      expect(component.isPublicView).toBe(false);
      expect(component.selectedFilters.has(PublishStatus.Draft)).toBe(true);
      expect(component.selectedFilters.has(PublishStatus.Published)).toBe(true);
    });

    it('should enable skill selection when user is authenticated', () => {
      // Arrange
      const authService = TestBed.inject(AuthService);
      spyOn(authService, 'isAuthenticated').and.returnValue(true);

      // Act
      component.ngOnInit();

      // Assert
      expect(component.getSelectAllEnabled()).toBe(true);
    });
  });

  describe('public mode behavior', () => {
    beforeEach(() => {
      const authService = TestBed.inject(AuthService);
      spyOn(authService, 'isAuthenticated').and.returnValue(false);
      component.ngOnInit();
    });

    it('should disable all mutation actions in public view', () => {
      expect(component.publishVisible()).toBe(false);
      expect(component.archiveVisible()).toBe(false);
      expect(component.unarchiveVisible()).toBe(false);
      expect(component.addToCollectionVisible()).toBe(false);
      expect(component.addToWorkspaceVisiblePublic).toBe(false);
      expect(component.exportSearchVisiblePublic).toBe(false);
    });

    it('should disable action bar in public view', () => {
      expect(component.actionsVisible()).toBe(false);
    });

    it('should disable skill selection in public view', () => {
      expect(component.getSelectAllEnabled()).toBe(false);
    });
  });
});
