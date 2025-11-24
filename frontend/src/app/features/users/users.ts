import {Component, signal} from '@angular/core';
import {DataTable} from '../../shared/components/data-table/data-table';
import {UsersService} from './users.service';

@Component({
  selector: 'app-users',
  imports: [
    DataTable
  ],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users {
  constructor(private usersService: UsersService) {
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
}
