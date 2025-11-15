import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddUsersModal } from './users-modal';

describe('AddUsersModal', () => {
  let component: AddUsersModal;
  let fixture: ComponentFixture<AddUsersModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddUsersModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddUsersModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
