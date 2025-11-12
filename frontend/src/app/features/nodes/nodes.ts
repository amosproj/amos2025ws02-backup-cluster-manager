import {Component, OnInit, signal} from '@angular/core';
import {NodesService} from './nodes.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';

@Component({
  selector: 'app-nodes',
  imports: [
    AsyncPipe,
    DataTable
  ],
  templateUrl: './nodes.html',
  styleUrl: './nodes.css',
})
export class Nodes implements OnInit {
  nodes = signal<any[]>([]);
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  // Columns to be included in search
  tableSearchColumns = signal(['name', 'status', 'id']);

  // Example filter: filter nodes by 'active' status
  tableFilters = signal([
    {
      label: 'Active',
      filterFn: (item:any) => item.status.toLowerCase() === "active",
      active: false,
    }
  ]);
  error = signal<string | null>(null);
  loading$;

  constructor(
    private nodesService: NodesService,
    private apiService: ApiService
  ) {
    this.loading$ = this.apiService.loading$;
  }

  ngOnInit() {
    this.loadNodes();
  }

  loadNodes() {
    this.error.set(null);
    this.nodesService.getNodes().subscribe({
      next: (data) => this.nodes.set(data),
      error: (error) => this.error.set(error.message)
    })
  }
}
