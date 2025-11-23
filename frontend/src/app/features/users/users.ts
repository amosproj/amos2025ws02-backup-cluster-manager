import { Component } from '@angular/core';
import { UsersModal } from '../../shared/components/users-modal/users-modal';

@Component({
  selector: 'app-users',
  imports: [UsersModal],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  isAddUserModalOpen = false;
  modalMode: 'create' | 'edit' | 'delete' = 'create';

  openAddUserModal(mode: 'create' | 'edit' | 'delete') {
    this.modalMode = mode;
    this.isAddUserModalOpen = true;
  }
}
