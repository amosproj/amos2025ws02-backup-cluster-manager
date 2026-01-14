import {Component, signal, ViewChild, OnInit, OnDestroy} from '@angular/core';
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
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import { UsersModal } from '../../shared/components/users-modal/users-modal';
import { ToastTypeEnum } from '../../shared/types/toast';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-backups',
  imports: [
    AsyncPipe,
    DataTable,
    ReactiveFormsModule,
    UsersModal
  ],
  templateUrl: './backups.html',
  styleUrl: './backups.css',
})
export class Backups implements OnInit, OnDestroy {
  @ViewChild(DataTable) dataTable!: DataTable;
  private refreshSub?: Subscription;


  tableColumns = signal([
    { field: 'id', header: 'ID' },
    { field: 'address', header: 'Node'},
    { field: 'clientId', header: 'Client ID' },
    { field: 'taskId', header: 'Task ID' },
    { field: 'sizeBytes', header: 'Size (Bytes)' },
    { field: 'state', header: 'State' },
    { field: 'startTime', header: 'Start Time' },
    { field: 'stopTime', header: 'End Time' }
  ]);
  clients = signal<any[]>([]);
  tasks = signal<any[]>([]);


  isAddBackupModalOpen = false;
  refreshTrigger = signal(0);
  modalMode: 'backups' = 'backups';
  openAddBackupModal(mode: 'backups' ) {
    this.modalMode = mode;
    this.isAddBackupModalOpen = true;
  }
  onModalClosed() {
    this.isAddBackupModalOpen = false;
    this.refreshTrigger.update(value => value + 1);
  }

  selectedBackups: any[] = [];
  onSelectionChange(rows: any[]): void {
    this.selectedBackups = rows;
  }

  onDeleteSelection(rows: any[]): void {
    if (!rows.length) return;

    let completed = 0;

    rows.forEach(row => {
      this.backupsService.deleteBackup(row.id, row.nodeDTO.address).subscribe({
        next: () => {
          completed++;

          if (completed === rows.length && this.dataTable) {
            this.dataTable.loadData();
          }
          this.toast.show('Backup deleted successfully!', ToastTypeEnum.SUCCESS);
        },
        error: (error) => {
          this.toast.show('Error deleting backup!', ToastTypeEnum.ERROR);
          console.error('Error deleting backup:', error);
        }
      });
    });
  }
  //  filter
  tableFilters = signal([
    {
      label: 'Completed',
      active: false,
    },
    {
      label: 'Canceled',
      active: false,
    },
    {
      label: 'Failed',
      active: false,
    },
    {
      label: "Queued",
      active: false,
    },
    {
      label: "Running",
      active: false,
    }
  ]);
  loading$;

  showAddModal = signal(false);
  addForm!: FormGroup;

  constructor(
    private backupsService: BackupsService,
    private clientsService: ClientsService,
    private tasksService: TasksService,
    private apiService: ApiService,
    private fb: FormBuilder,
    public authService: AuthService,
    private autoRefreshService: AutoRefreshService,
    public toast: ToastService
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
    pipe(map((response: PaginatedResponse) => {
        const mappedResponse = {
          ...response, // keep currentPage and totalPages
          items: response.items.map((backup: any) => ({
            ...backup,
            address: backup.nodeDTO?.address
          }))
        };

        return formatDateFields(mappedResponse, ['startTime', 'stopTime']);
      })
    );
  };


  openAddModal() {
    this.addForm.reset();
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
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
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
    }
  }

  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
