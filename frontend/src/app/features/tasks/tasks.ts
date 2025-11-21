import {Component, OnInit, signal} from '@angular/core';
import {TasksService } from './tasks.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';

@Component({
  selector: 'app-tasks',
  imports: [
    AsyncPipe,
    DataTable
  ],
  templateUrl: './tasks.html',
  styleUrl: './tasks.css',
})
export class Tasks implements OnInit {
  tasks = signal<any[]>([]);
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'clientId', header: 'ClientID'},
    {field: 'source', header: 'Source'},
    {field: 'enabled', header: 'Enabled'},
    {field: 'interval', header: 'Interval'},
  ]);

  // Columns to be included in search
  tableSearchColumns = signal(['name', 'enabled', 'id', "clientId"]);

  // Example filter: filter tasks by 'active' status
  tableFilters = signal([
    {
      label: 'Enabled',
      filterFn: (item:any) => item.enabled === true,
      active: false,
    }
  ]);
  error = signal<string | null>(null);
  loading$;

  constructor(
    private tasksService: TasksService,
    private apiService: ApiService
  ) {
    this.loading$ = this.apiService.loading$;
  }

  ngOnInit() {
    this.loadTasks();
  }

  loadTasks() {
    this.error.set(null);
    this.tasksService.getTasks().subscribe({
      next: (data) => this.tasks.set(data),
      error: (error) => this.error.set(error.message)
    })
  }
}
