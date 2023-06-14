import { TestBed } from '@angular/core/testing';

import { NotRenewPasswordGuard } from './not-renew-password.guard';

describe('NotRenewPasswordGuard', () => {
  let guard: NotRenewPasswordGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(NotRenewPasswordGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
