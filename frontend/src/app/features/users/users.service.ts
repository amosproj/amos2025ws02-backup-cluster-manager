import { Injectable } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Observable } from 'rxjs';

export interface User {
  name: string;
  password: string;
  enabled: boolean;
}

export interface CreateUserPayload {
  name: string;
  passwordHash: string; // hashed or plain to be hashed server-side
  enabled: boolean;
}

export interface UpdateUserPayload {
  name?: string;
  passwordHash?: string;
  enabled: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  constructor(private api: ApiService) {}

  // Create (POST /users)
  createUser(roleId: number | string, payload: CreateUserPayload): Observable<User> {
    console.log('Creating user with roleId:', roleId, 'and payload:', payload); 
    return this.api.post<User>(`users/${roleId}`, payload);
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