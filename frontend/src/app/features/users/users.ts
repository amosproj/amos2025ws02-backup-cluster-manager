import { Component, signal } from '@angular/core';
import { UsersModal } from '../../shared/components/users-modal/users-modal';
import {DataTable} from '../../shared/components/data-table/data-table';
import {UsersService} from './users.service';
import {AuthService} from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';

@Component({
  selector: 'app-users',
  imports: [DataTable, UsersModal],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  constructor(private usersService: UsersService, public authService: AuthService) {
  }

  isAddUserModalOpen = false;
  modalMode: 'create' | 'edit' | 'delete' = 'create';

  openAddUserModal(mode: 'create' | 'edit' | 'delete') {
    this.modalMode = mode;
    this.isAddUserModalOpen = true;
  }

  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'enabled', header: 'Enabled'},
    {field: 'createdAt', header: 'Created At'},
    {field: 'updatedAt', header: 'Updated At'},
  ]);

  tableFilters = signal([
    {
      label: 'Enabled',
      active: false,
    },
    {
      label: 'Disabled',
      active: false,
    }
  ]);

  fetchUsers = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder: string) => {
    return this.usersService.getUsers(page, itemsPerPage, filters, search, sortBy, sortOrder);
  }
  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
