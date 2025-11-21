import {Component, OnInit, signal} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {BackupsService} from './backups.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {SortOrder} from '../../shared/types/SortTypes';

@Component({
  selector: 'app-backups',
  imports: [
    DataTable
  ],
  templateUrl: './backups.html',
  styleUrl: './backups.css',
})
export class Backups {
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  // Example filter: filter backups by 'active' status
  tableFilters = signal([
    {
      label: 'Active',
      filterFn: (item: any) => item.status.toLowerCase() === "active",
      active: false,
    }
  ]);

  constructor(
    private backupsService: BackupsService,
  ) {}

  fetchBackups = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder:SortOrder) => {
    return this.backupsService.getBackups(page, itemsPerPage, filters, search, sortBy, sortOrder);
  }
}
