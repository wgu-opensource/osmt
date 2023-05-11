import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NamedReferenceListRowComponent } from './named-reference-list-row.component';

describe('NamedReferenceListRowComponent', () => {
  let component: NamedReferenceListRowComponent;
  let fixture: ComponentFixture<NamedReferenceListRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NamedReferenceListRowComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NamedReferenceListRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
