import {Component, OnInit, signal} from '@angular/core';
import {NodesService} from './nodes.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {SortOrder} from '../../shared/types/SortTypes';

@Component({
  selector: 'app-nodes',
  imports: [
    DataTable
  ],
  templateUrl: './nodes.html',
  styleUrl: './nodes.css',
})
export class Nodes {
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'address', header: 'Address'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  // Example filter: filter backups by 'active' status
  tableFilters = signal([
    {
      label: 'Active',
      active: false,
    },
    {
      label: 'Inactive',
      active: false,
    }
  ]);

  constructor(
    private nodesService: NodesService,
  ) {}

  fetchNodes = (page: number, itemsPerPage: number, filters: string, search:string, sortBy: string, sortOrder:SortOrder) => {
    return this.nodesService.getNodes(page, itemsPerPage, filters, search, sortBy, sortOrder);
  }
}
