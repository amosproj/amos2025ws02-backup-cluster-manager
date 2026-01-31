import {Component, inject} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {LoginService} from './login.service';
import {ToastService} from '../../core/services/toast.service';
import {ToastTypeEnum} from '../../shared/types/toast';
import {Router} from '@angular/router';

/**
 * Login page: form submit calls login service and navigates on success.
 */
@Component({
  selector: 'app-login',
  imports: [
    FormsModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  constructor(
    private loginService: LoginService,
    private toast:ToastService,
    private router: Router
  ){}

  /** Submits login form; on success navigates to home and shows toast. */
  login(form: NgForm){
    if (form.valid) {
      const { username, password } = form.value;
      this.loginService.login(username, password).subscribe({
        next: (res) => {
          // Handle successful login, e.g., navigate to dashboard
          console.log('Login successful:', res);
          this.toast.show("Login successful!", ToastTypeEnum.SUCCESS);
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Login failed:', error);
          // Handle login error, e.g., show error message
          this.toast.show("Login failed. Please check your credentials.", ToastTypeEnum.ERROR);
        }
      });
    }
  }
}
