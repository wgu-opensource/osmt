import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportRsdComponent } from './import-rsd.component';

describe('ImportRsdComponent', () => {
  let component: ImportRsdComponent;
  let fixture: ComponentFixture<ImportRsdComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ImportRsdComponent]
    });
    fixture = TestBed.createComponent(ImportRsdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
