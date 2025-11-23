import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsersModal } from './users-modal';

describe('UsersModal', () => {
  let component: UsersModal;
  let fixture: ComponentFixture<UsersModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsersModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsersModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
