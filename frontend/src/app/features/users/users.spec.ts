import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { Users } from './users';

describe('Users', () => {
  let component: Users;
  let fixture: ComponentFixture<Users>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Users],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Users);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open modal in create mode when openAddUserModal is called with "create"', () => {
    expect(component.isAddUserModalOpen).toBe(false);
    expect(component.modalMode).toBe('create');

    component.openAddUserModal('create');

    expect(component.isAddUserModalOpen).toBe(true);
    expect(component.modalMode).toBe('create');
  });

  it('should open modal in edit mode when openAddUserModal is called with "edit"', () => {
    component.openAddUserModal('edit');

    expect(component.isAddUserModalOpen).toBe(true);
    expect(component.modalMode).toBe('edit');
  });

  it('should open modal in delete mode when openAddUserModal is called with "delete"', () => {
    component.openAddUserModal('delete');

    expect(component.isAddUserModalOpen).toBe(true);
    expect(component.modalMode).toBe('delete');
  });
});
