import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkillImportComponent } from './skill-import.component';

describe('SkillImportComponent', () => {
  let component: SkillImportComponent;
  let fixture: ComponentFixture<SkillImportComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SkillImportComponent]
    });
    fixture = TestBed.createComponent(SkillImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
