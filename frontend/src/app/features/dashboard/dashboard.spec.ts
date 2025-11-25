import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { Dashboard } from './dashboard';
describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        { provide: Router, useValue: mockRouter },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
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

  it('should show confirmation on logout', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.onLogout();
    expect(window.confirm).toHaveBeenCalled();
  });

});
