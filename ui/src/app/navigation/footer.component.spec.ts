import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FooterComponent } from './footer.component';
import { AppConfig } from '../app.config';

describe('FooterComponent', () => {
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FooterComponent],
      imports: [CommonModule, HttpClientModule],
      providers: [AppConfig],
    }).compileComponents();
  });

  beforeEach(() => {
    const appConfig = TestBed.inject(AppConfig);
    AppConfig.settings = appConfig.defaultConfig();
    fixture = TestBed.createComponent(FooterComponent);
    fixture.detectChanges();
  });

  it('hides powered-by when poweredBy is empty', () => {
    const tagline = fixture.nativeElement.querySelector('.m-footer-x-tagline');
    expect(tagline).toBeNull();
  });

  it('shows powered-by when poweredBy is set', () => {
    AppConfig.settings.poweredBy = 'Powered by';
    AppConfig.settings.poweredByUrl = 'https://example.com';
    AppConfig.settings.poweredByLabel = 'Example';
    fixture.detectChanges();
    const tagline = fixture.nativeElement.querySelector('.m-footer-x-tagline');
    expect(tagline).toBeTruthy();
  });
});
