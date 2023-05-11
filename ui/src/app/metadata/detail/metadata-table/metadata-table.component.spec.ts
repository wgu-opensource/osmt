import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataTableComponent } from './metadata-table.component';

describe('MetadataTableComponent', () => {
  let component: MetadataTableComponent;
  let fixture: ComponentFixture<MetadataTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
