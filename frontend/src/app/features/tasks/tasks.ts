import {Component, OnInit, signal, ViewChild} from '@angular/core';
import {TasksService } from './tasks.service';
import {ClientDTO, ClientsService} from '../clients/clients.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import {SortOrder} from '../../shared/types/SortTypes';
import {map} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import { AuthService } from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';

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
    {field: 'address', header: 'Node'},
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

  trackClient = (_: number, client: ClientDTO) =>
    `${client.id}-${client.nodeDTO.id}`;

  ngOnInit() {
    this.clientsService.getClientList().subscribe({
      next: (data) => this.clients.set(data),
      error: (err) => console.error('Fehler beim Laden der Clients:', err)
    });
  }

  constructor(
    private tasksService: TasksService,
    private clientsService: ClientsService,
    private fb: FormBuilder,
    private apiService: ApiService,
    public authService: AuthService
  ) {
    this.loading$ = this.apiService.loading$;

    this.addForm = this.fb.group({
      name: ['', Validators.required],
      source: ['', Validators.required],
      enabled: [true, Validators.required],
      interval: ['', Validators.required],
      clientSelection: [null, Validators.required],
    });
  }

  fetchTasks = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder:SortOrder) => {
    return this.tasksService.getTasks(page, itemsPerPage, filters, search, sortBy, sortOrder).pipe(
      map((response: PaginatedResponse) => ({
        ...response, // keep currentPage and totalPages
        items: response.items.map((task: any) => ({
          ...task,                       // keep all task fields
          address: task.nodeDTO?.address // flatten node address
        }))
      }))
    );
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
    const { clientSelection, ...taskData } = this.addForm.value;

    this.tasksService.createTask({id: null, ...clientSelection, ...taskData}).subscribe({
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

  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
