import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Login } from './login';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { LoginService } from './login.service';
import { ToastService } from '../../core/services/toast.service';
import { Router } from '@angular/router';
import { NgForm } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ToastTypeEnum } from '../../shared/types/toast';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let loginService: jasmine.SpyObj<LoginService>;
  let toastService: jasmine.SpyObj<ToastService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create spy objects for dependencies
    const loginServiceSpy = jasmine.createSpyObj('LoginService', ['login']);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['show']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LoginService, useValue: loginServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    loginService = TestBed.inject(LoginService) as jasmine.SpyObj<LoginService>;
    toastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('login method', () => {
    let mockForm: jasmine.SpyObj<NgForm>;

    beforeEach(() => {
      mockForm = jasmine.createSpyObj('NgForm', [], {
        valid: true,
        value: { username: 'testuser', password: 'testpass' }
      });
    });

    it('should call loginService.login with correct credentials when form is valid', () => {
      loginService.login.and.returnValue(of(true));

      component.login(mockForm);

      expect(loginService.login).toHaveBeenCalledWith('testuser', 'testpass');
    });

    it('should show success toast and navigate to home on successful login', () => {
      loginService.login.and.returnValue(of(true));

      component.login(mockForm);

      expect(toastService.show).toHaveBeenCalledWith('Login successful!', ToastTypeEnum.SUCCESS);
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should show error toast on failed login', () => {
      const error = { status: 401, message: 'Unauthorized' };
      loginService.login.and.returnValue(throwError(() => error));

      component.login(mockForm);

      expect(toastService.show).toHaveBeenCalledWith(
        'Login failed. Please check your credentials.',
        ToastTypeEnum.ERROR
      );
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should not call loginService when form is invalid', () => {
      Object.defineProperty(mockForm, 'valid', { value: false });

      component.login(mockForm);

      expect(loginService.login).not.toHaveBeenCalled();
      expect(toastService.show).not.toHaveBeenCalled();
    });
  });
});
