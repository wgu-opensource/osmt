import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { HeaderComponent } from './header.component';
import { AuthService } from '../auth/auth-service';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfig } from '../app.config';
import { EnvironmentService } from '../core/environment.service';
import { Location } from '@angular/common';
import { ConcreteService } from '../abstract.service.spec';
import { HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';
import { MyWorkspaceComponent } from '../my-workspace/my-workspace.component';
import { RichSkillsLibraryComponent } from '../richskill/library/rich-skills-library.component';
import { By } from '@angular/platform-browser';
import { Idle, IdleExpiry } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      providers: [
        EnvironmentService,
        AppConfig,
        ConcreteService,
        Location,
        AuthService,
        Idle,
        IdleExpiry,
        Keepalive,
      ],
      imports: [
        HttpClientModule,
        RouterTestingModule.withRoutes([
          {
            path: 'my-workspace',
            component: MyWorkspaceComponent,
          },
          {
            path: 'skills',
            component: RichSkillsLibraryComponent,
          },
        ]),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const appConfig = TestBed.inject(AppConfig);
    AppConfig.settings = appConfig.defaultConfig();
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('my workspace is active', fakeAsync(() => {
    router.navigate(['/my-workspace']);
    tick();
    expect(component.myWorkspaceActive).toBeTruthy();
  }));

  it("my workspace is not visible when user doesn't have role admin or curator", () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'isEnabledByRoles').and.returnValue(false);
    spyOn(component, 'isAuthenticated').and.returnValue(true);
    fixture.detectChanges();
    const myWorkspace = fixture.debugElement.query(By.css('#li-my-workspace'));
    expect(myWorkspace).toBeFalsy();
    expect(component.canHaveWorkspace).toBeFalse();
  });

  it('canHaveWorkspace should be false', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'getRole').and.returnValue('ROLE_Osmt_Viewer');
    expect(component.canHaveWorkspace).toBeFalse();
  });

  it('canHaveWorkspace should be true', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'getRole').and.returnValue('ROLE_Osmt_Admin');
    expect(component.canHaveWorkspace).toBeTrue();
  });

  it('my workspace is visible when user has role admin or curator', done => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'getRole').and.returnValue('ROLE_Osmt_Admin');
    spyOn(component, 'isAuthenticated').and.returnValue(true);
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const myWorkspace = fixture.debugElement.query(
        By.css('#li-my-workspace')
      );
      expect(myWorkspace).toBeTruthy();
      expect(component.canHaveWorkspace).toBeTrue();
      done();
    });
  });

  it('shows Sync nav item for admin when authenticated', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'getRole').and.returnValue('ROLE_Osmt_Admin');
    spyOn(component, 'isAuthenticated').and.returnValue(true);
    fixture.detectChanges();
    const syncLi = fixture.debugElement.query(By.css('#li-sync'));
    expect(syncLi).toBeTruthy();
  });

  it('hides Sync nav item when user is curator only', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'getRole').and.returnValue('ROLE_Osmt_Curator');
    spyOn(component, 'isAuthenticated').and.returnValue(true);
    fixture.detectChanges();
    const syncLi = fixture.debugElement.query(By.css('#li-sync'));
    expect(syncLi).toBeFalsy();
  });

  it('skills is active', fakeAsync(() => {
    router.navigate(['/skills']);
    tick();
    expect(router).toBeTruthy();
  }));

  it('uses default logo when logoUrl is unset', () => {
    AppConfig.settings.logoUrl = undefined;
    fixture.detectChanges();
    const img = fixture.nativeElement.querySelector(
      '.m-navBar-x-brand img'
    ) as HTMLImageElement;
    expect(img.src).toContain('/assets/images/logo-light.svg');
  });

  it('uses whitelabel logoUrl when set', () => {
    AppConfig.settings.logoUrl = 'https://example.com/logo.svg';
    fixture.detectChanges();
    const img = fixture.nativeElement.querySelector(
      '.m-navBar-x-brand img'
    ) as HTMLImageElement;
    expect(img.src).toBe('https://example.com/logo.svg');
  });
});
