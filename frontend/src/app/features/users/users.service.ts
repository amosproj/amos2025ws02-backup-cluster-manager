import { Injectable } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Observable } from 'rxjs';

export interface User {
  name: string;
  password: string;
  status: 'enabled' | 'disabled';
}

export interface CreateUserPayload {
  name: string;
  passwordHash: string;
  status: 'enabled' | 'disabled';
}

export interface UpdateUserPayload {
  name?: string;
  passwordHash?: string;
  status?: 'enabled' | 'disabled';
}

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  constructor(private api: ApiService) {}

  // Create (POST /users)
  createUser(payload: CreateUserPayload): Observable<User> {
    console.log('Creating user with payload:', payload);
    return this.api.post<User>('users', payload);
  }

  // Update (PUT /users/:id)
  updateUser(id: number | string, payload: UpdateUserPayload): 
  Observable<User> {
    return this.api.put<User>(`users/${id}`, payload);
  }

  // Delete (DELETE /users/:id)
  deleteUser(id: number | string): Observable<void> {
    return this.api.delete<void>(`users/${id}`);
  }
}