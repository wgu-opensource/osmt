import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNamedReferenceComponent } from './create-named-reference.component';

describe('CreateComponent', () => {
  let component: CreateNamedReferenceComponent;
  let fixture: ComponentFixture<CreateNamedReferenceComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CreateNamedReferenceComponent]
    });
    fixture = TestBed.createComponent(CreateNamedReferenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
