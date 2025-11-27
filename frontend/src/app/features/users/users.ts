import {Component, EventEmitter, signal, ViewChild} from '@angular/core';
import {UsersModal} from '../../shared/components/users-modal/users-modal';
import {DataTable} from '../../shared/components/data-table/data-table';
import {UsersService} from './users.service';
import {firstValueFrom} from 'rxjs';

@Component({
  selector: 'app-users',
  imports: [DataTable, UsersModal],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  @ViewChild(DataTable) dataTable!: DataTable;
  constructor(private usersService: UsersService) {
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

  onDeleteSelection =async (rows: any[]) => {
    const deletions = rows.map((row:any) => firstValueFrom(this.usersService.deleteUser(row.id)));
    await Promise.all(deletions);
    this.dataTable.loadData();
  }

  onUserDataChanges(){
    this.dataTable.loadData();
  }
}
