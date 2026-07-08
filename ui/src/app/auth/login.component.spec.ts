import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from './auth-service';
import { AppConfig } from '../app.config';
import { DefaultAppConfig } from '../models/app-config.model';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [FormsModule],
      providers: [
        {
          provide: Router,
          useValue: { navigate: jasmine.createSpy('navigate') },
        },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) },
        },
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: jasmine
              .createSpy('isAuthenticated')
              .and.returnValue(false),
          },
        },
      ],
    }).compileComponents();

    AppConfig.settings = Object.assign(new DefaultAppConfig(), {
      baseApiUrl: '/api',
    });

    fixture = TestBed.createComponent(LoginComponent);
  });

  it('should treat read-only instance without login form', () => {
    AppConfig.settings.readOnlyMode = true;
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Public skill browser');
    expect(el.textContent).not.toContain(
      'Sign in with your organization account'
    );
  });

  it('should show normal sign-in when writable', () => {
    AppConfig.settings.readOnlyMode = false;
    AppConfig.settings.authProviders = [
      {
        id: 'google',
        name: 'Google',
        authorizationUrl: 'https://example.com/oauth',
      },
    ];
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Sign In');
  });
});
