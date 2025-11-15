import { Component } from '@angular/core';
import { AddUsersModal } from '../../shared/components/add-users-modal/add-users-modal';

@Component({
  selector: 'app-users',
  imports: [AddUsersModal],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  
}
