import { Component, signal } from '@angular/core';
import { UsersModal } from '../../shared/components/users-modal/users-modal';
import { DataTable } from '../../shared/components/data-table/data-table';
import { UsersService } from './users.service';
import { AuthService } from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';
import {map} from 'rxjs';
import {formatDateFields} from '../../shared/utils/date_utils';

/**
 * Users feature: paginated user list, add/edit/delete via modal.
 */
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
  selectedUser: any | null = null;
  refreshTrigger = signal(0);

  openAddUserModal(mode: 'create' ) {
    this.modalMode = mode;
    this.isAddUserModalOpen = true;
  }

  openModifyUserModal(mode: 'edit' | 'delete', user: any) {
    this.modalMode = mode;
    this.selectedUser = user || null;
    this.isAddUserModalOpen = true;
  }

  onModalClosed() {
    this.isAddUserModalOpen = false;
    this.refreshTrigger.update(value => value + 1);
  }


  tableColumns = signal([
    { field: 'id', header: 'ID' },
    { field: 'name', header: 'Name' },
    { field: 'enabled', header: 'Enabled' },
    { field: 'createdAt', header: 'Created At' },
    { field: 'updatedAt', header: 'Updated At' },
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
    return this.usersService.getUsers(page, itemsPerPage, filters, search, sortBy, sortOrder)
      .pipe(
        map(response => formatDateFields(response, ['createdAt', 'updatedAt']))
      );
  }
  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
