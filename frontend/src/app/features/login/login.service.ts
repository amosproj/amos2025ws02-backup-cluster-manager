import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {AuthService} from '../../core/services/auth.service';
import {NgForm} from '@angular/forms';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  constructor(private authService: AuthService) {
  }

  login(username: string, password: string) {
    return this.authService.login(username, password);
  }

}
