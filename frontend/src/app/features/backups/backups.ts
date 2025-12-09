import {Component, signal, ViewChild, OnInit} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {BackupsService} from './backups.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {SortOrder} from '../../shared/types/SortTypes';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {map} from 'rxjs';
import {formatDateFields} from '../../shared/utils/date_utils';
import {ClientsService} from '../clients/clients.service';
import {TasksService} from '../tasks/tasks.service';

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
    private fb: FormBuilder
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
    this.clientsService.getClients().subscribe({
      next: (data) => this.clients.set(data),
      error: (err) => console.error('Fehler beim Laden der Clients:', err)
    });
    this.tasksService.getTasks().subscribe({
      next: (response) => this.tasks.set(response.items),
      error: (err) => console.error('Fehler beim Laden der Tasks:', err)
    });

    this.refreshIntervalId = setInterval(() => {
      if (this.dataTable) {
        this.dataTable.loadData();
      }
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

}
