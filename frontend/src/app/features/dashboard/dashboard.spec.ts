import { ComponentFixture, TestBed } from '@angular/core/testing';
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
        { provide: Router, useValue: mockRouter }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to users on add-user button click', () => {
    component.onButtonClick('add-user');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/users']);
  });

  it('should navigate to users on edit-user button click', () => {
    component.onButtonClick('edit-user');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/users']);
  });

  it('should show confirmation on logout', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    component.onLogout();
    expect(window.confirm).toHaveBeenCalled();
  });

});
