import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {AuthService} from '../../core/services/auth.service';
import {NgForm} from '@angular/forms';

/**
 * Login facade: delegates to AuthService.login for form-based login.
 */
@Injectable({
  providedIn: 'root',
})
export class LoginService {
  constructor(private authService: AuthService) {
  }

  /** Logs in with the given credentials. */
  login(username: string, password: string) {
    return this.authService.login(username, password);
  }

}
