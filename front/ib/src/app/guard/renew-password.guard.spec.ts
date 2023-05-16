import { TestBed } from '@angular/core/testing';

import { RenewPasswordGuard } from './renew-password.guard';

describe('RenewPasswordGuard', () => {
  let guard: RenewPasswordGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(RenewPasswordGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
