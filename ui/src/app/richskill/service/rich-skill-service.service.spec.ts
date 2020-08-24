import { TestBed } from '@angular/core/testing';

import { RichSkillServiceService } from './rich-skill-service.service';

describe('RichSkillServiceService', () => {
  let service: RichSkillServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RichSkillServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
