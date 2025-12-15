import {Component, signal, ViewChild, OnInit} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {BackupsService} from './backups.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {SortOrder} from '../../shared/types/SortTypes';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {map, Subscription} from 'rxjs';
import {formatDateFields} from '../../shared/utils/date_utils';
import {AuthService} from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';
import {ClientsService} from '../clients/clients.service';
import {TasksService} from '../tasks/tasks.service';
import {AutoRefreshService} from '../../services/dynamic-page';

@Component({
  selector: 'app-backups',
  imports: [
    AsyncPipe,
    DataTable,
    ReactiveFormsModule
  ],
  templateUrl: './backups.html',
  styleUrl: './backups.css',
})
export class Backups {
  @ViewChild(DataTable) dataTable!: DataTable;
  private refreshSub?: Subscription;

  tableColumns = signal([
    { field: 'id', header: 'ID' },
    { field: 'clientId', header: 'Client ID' },
    { field: 'taskId', header: 'Task ID' },
    { field: 'sizeBytes', header: 'Size (Bytes)' },
    { field: 'state', header: 'State' },
    { field: 'startTime', header: 'Start Time' },
    { field: 'stopTime', header: 'End Time' }
  ]);
  clients = signal<any[]>([]);
  tasks = signal<any[]>([]);

  selectedBackups: any[] = [];
  onSelectionChange(rows: any[]): void {
    this.selectedBackups = rows;
  }

  onDeleteSelection(rows: any[]): void {
    if (!rows.length) return;

    const ids = rows.map(r => r.id);
    let completed = 0;

    ids.forEach(id => {
      this.backupsService.deleteBackup(id).subscribe({
        next: () => {
          completed++;

          // Refresh only once, after last item is deleted
          if (completed === ids.length) {
            if (this.dataTable) {
              this.dataTable.loadData();
            }
          }
        },
        error: (error) => {
          console.error('Error deleting backup:', error);
        }
      });
    });
  }
  //  filter
  tableFilters = signal([
    {
      filterFn: (item: any) => item.state?.toLowerCase() === "completed",
      label: 'Completed',
      active: false,
    }
  ]);
  loading$;

  showAddModal = signal(false);
  addForm!: FormGroup;

  private refreshIntervalId: any;

  constructor(
    private backupsService: BackupsService,
    private clientsService: ClientsService,
    private tasksService: TasksService,
    private apiService: ApiService,
    private fb: FormBuilder,
    public authService: AuthService,
    private autoRefreshService: AutoRefreshService
  ) {
    this.loading$ = this.apiService.loading$;

    this.addForm = this.fb.group({
      clientId: ['', Validators.required],
      taskId: ['', Validators.required],
      sizeBytes: ['', Validators.required],
    });

  }

  fetchBackups = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder: SortOrder) => {
    return this.backupsService.getBackups(page, itemsPerPage, filters, search, sortBy, sortOrder).
    pipe(map((result: any) => formatDateFields(result, ['startTime', 'stopTime'])));
  };

  openAddModal() {
    this.addForm.reset();
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
  }

  submitAddBackup() {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }

    this.apiService.post('backup', this.addForm.value).subscribe({
      next: () => {
        this.closeAddModal();

      },
    });

    this.backupsService.createBackup(this.addForm.value).subscribe({
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

  ngOnInit(): void {
    this.clientsService.getClientList().subscribe({
      next: (data) => this.clients.set(data),
      error: (err) => console.error('Fehler beim Laden der Clients:', err)
    });
    this.tasksService.getTaskList().subscribe({
      next: (data) => this.tasks.set(data),
      error: (err) => console.error('Fehler beim Laden der Tasks:', err)
    });
    this.refreshSub = this.autoRefreshService.refresh$.subscribe(() => {
      if (!this.showAddModal() && this.dataTable) {
        this.dataTable.loadData();
      }
    });

  }

  ngOnDestroy(): void {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
