import { Component, signal } from '@angular/core';
import { UsersService, CreateUserPayload, UpdateUserPayload } from './users.service';
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
  selectedUser: any | null = null;

  constructor(private usersService: UsersService) {}

  openCreateModal() {
    this.modalMode = 'create';
    this.selectedUser = null;
    this.isAddUserModalOpen = true;
  }

  openEditModal(user: any) {
    this.modalMode = 'edit';
    this.selectedUser = user;
    this.isAddUserModalOpen = true;
  }

  openDeleteModal(user: any) {
    this.modalMode = 'delete';
    this.selectedUser = user;
    this.isAddUserModalOpen = true;
  }

  onUserSubmitted(payload: any) {
    if (this.modalMode === 'create') {
      // Call the service and SUBSCRIBE
      this.usersService.createUser(payload as CreateUserPayload).subscribe({
        next: (response) => {
          // console.log('[Users Component] User created successfully:', response);
          // Reload users list or add to local array
        },
        error: (err) => {
          // console.error('[Users Component] Error creating user:', err);
        }
      });
    } else if (this.modalMode === 'edit') {
      const userId = this.selectedUser?.id;
      if (userId) {
        // Call the service and SUBSCRIBE
        this.usersService.updateUser(userId, payload as UpdateUserPayload).subscribe({
          next: (response) => {
            // console.log('[Users Component] User updated successfully:', response);
            // Update local users list
          },
          error: (err) => {
            // console.error('[Users Component] Error updating user:', err);
          }
        });
      }
    } else if (this.modalMode === 'delete') {
      const userId = this.selectedUser?.id;
      if (userId) {
        // Call the service and SUBSCRIBE
        this.usersService.deleteUser(userId).subscribe({
          next: () => {
            // console.log('[Users Component] User deleted successfully');
            // Remove from local users list
          },
          error: (err) => {
            // console.error('[Users Component] Error deleting user:', err);
          }
        });
      }
    }

    this.isAddUserModalOpen = false;
  }

  onModalClose() {
    this.isAddUserModalOpen = false;
  }
}