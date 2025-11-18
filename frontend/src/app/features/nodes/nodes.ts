import {Component, OnInit, signal} from '@angular/core';
import {NodesService} from './nodes.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {BackupsService} from '../backups/backups.service';

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
    {field: 'createdAt', header: 'Created At'},
  ]);
  // Columns to be included in search
  tableSearchColumns = signal(['name', 'status', 'id']);

  // Example filter: filter backups by 'active' status
  tableFilters = signal([
    {
      label: 'Active',
      filterFn: (item: any) => item.status.toLowerCase() === "active",
      active: false,
    }
  ]);

  constructor(
    private nodesService: NodesService,
  ) {}

  fetchNodes = (page: number, itemsPerPage: number) => {
    return this.nodesService.getNodes(page, itemsPerPage)
  }
}
