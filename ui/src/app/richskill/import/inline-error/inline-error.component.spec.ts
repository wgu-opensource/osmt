import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InlineErrorComponent } from './inline-error.component';

describe('InlineErrorComponent', () => {
  let component: InlineErrorComponent;
  let fixture: ComponentFixture<InlineErrorComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InlineErrorComponent]
    });
    fixture = TestBed.createComponent(InlineErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
