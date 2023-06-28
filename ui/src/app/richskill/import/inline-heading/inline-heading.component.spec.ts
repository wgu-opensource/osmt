import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InlineHeadingComponent } from './inline-heading.component';

describe('InlineheadingComponent', () => {
  let component: InlineHeadingComponent;
  let fixture: ComponentFixture<InlineHeadingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InlineHeadingComponent]
    });
    fixture = TestBed.createComponent(InlineHeadingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
