import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NamedReferenceTableComponent } from './named-reference-table.component';

describe('NamedReferenceTableComponent', () => {
  let component: NamedReferenceTableComponent;
  let fixture: ComponentFixture<NamedReferenceTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NamedReferenceTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NamedReferenceTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
