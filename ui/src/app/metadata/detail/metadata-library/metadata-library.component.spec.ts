import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataLibraryComponent } from './metadata-library.component';

describe('ManageMetadataComponent', () => {
  let component: MetadataLibraryComponent;
  let fixture: ComponentFixture<MetadataLibraryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataLibraryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataLibraryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
