import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BatchImportCollectionComponent } from './batch-import-collection.component';

describe('BatchImportCollectionComponent', () => {
  let component: BatchImportCollectionComponent;
  let fixture: ComponentFixture<BatchImportCollectionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [BatchImportCollectionComponent]
    });
    fixture = TestBed.createComponent(BatchImportCollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
