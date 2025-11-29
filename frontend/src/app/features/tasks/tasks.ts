import {Component, OnInit, signal, ViewChild} from '@angular/core';
import {TasksService } from './tasks.service';
import {ClientsService} from '../clients/clients.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import {SortOrder} from '../../shared/types/SortTypes';

@Component({
  selector: 'app-tasks',
  imports: [
    AsyncPipe,
    DataTable,
    ReactiveFormsModule
  ],
  templateUrl: './tasks.html',
  styleUrl: './tasks.css',
})
export class Tasks implements OnInit {
  @ViewChild(DataTable) dataTable!: DataTable;
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'clientId', header: 'ClientID'},
    {field: 'source', header: 'Source'},
    {field: 'enabled', header: 'Enabled'},
    {field: 'interval', header: 'Interval'},
  ]);

  // Example filter: filter tasks by 'active' status
  tableFilters = signal([
    {
      label: 'Enabled',
      filterFn: (item:any) => item.enabled === true,
      active: false,
    }
  ]);
  loading$;

  showAddModal = signal(false);
  addForm!: FormGroup;

  clients = signal<any[]>([]);
  intervalOptions = [
    { value: 'DAILY', label: 'Daily' },
    { value: 'WEEKLY', label: 'Weekly' },
    { value: 'MONTHLY', label: 'Monthly' }
  ];

  ngOnInit() {
    this.clientsService.getClients().subscribe({
      next: (data) => this.clients.set(data),
      error: (err) => console.error('Fehler beim Laden der Clients:', err)
    });
  }

  constructor(
    private tasksService: TasksService,
    private clientsService: ClientsService,
    private fb: FormBuilder,
    private apiService: ApiService
  ) {
    this.loading$ = this.apiService.loading$;

    this.addForm = this.fb.group({
      clientId: ['', Validators.required],
      name: ['', Validators.required],
      source: ['', Validators.required],
      enabled: [true, Validators.required],
      interval: ['', Validators.required],
    });
  }

  fetchTasks = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder:SortOrder) => {
    return this.tasksService.getTasks(page, itemsPerPage, filters, search, sortBy, sortOrder);
  }

  openAddModal() {
    this.addForm.reset();
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
  }

  submitTask() {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }

    this.tasksService.createTask({id: null, ...this.addForm.value}).subscribe({
      next: (response) => {
        //console.log('Backup created:', response);
        this.closeAddModal();
        if (this.dataTable) {
          this.dataTable.loadData();
        }
      },
      error: (error) => {
        console.error('Error creating backup:', error);
      }
    });

  }
}
