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
  constructor(private usersService: UsersService) {}
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'enabled', header: 'Enabled'},
    {field: 'updatedAt', header: 'Updated At'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  tableSearchColumns = signal(["name", "id"]);

  tableFilters = signal([
    {
      label: 'Enabled',
      filterFn: (item: any) => item.enabled === true,
      active: false,
    },
  ])

  fetchUsers = (page: number, itemsPerPage: number, search?: string) => {
    return this.usersService.getUsers(page, itemsPerPage, search || '')
  }



}
