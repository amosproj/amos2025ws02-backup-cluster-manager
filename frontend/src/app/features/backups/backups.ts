import {Component, OnInit, signal} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {BackupsService} from './backups.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {ReactiveFormsModule, FormBuilder, Validators, FormGroup} from '@angular/forms';

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
export class Backups implements OnInit {
  backups = signal<any[]>([]);
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  onAddBackup = () => this.openAddModal();

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
  error = signal<string | null>(null);
  loading$;

  // Modal-State + Reactive Form
  showAddModal = signal(false);
  addForm!: FormGroup;

  constructor(
    private backupsService: BackupsService,
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

  ngOnInit() {
    this.loadBackups();
  }

  loadBackups() {
    this.error.set(null);
    this.backupsService.getBackups().subscribe({
      next: (data) => this.backups.set(data),
      error: (error) => this.error.set(error.message)
    })
  }

  addBackup() {
    this.error.set(null);
    this.apiService.post('backup', {}).subscribe({
      next: () => this.loadBackups(),
      error: (error) => this.error.set(error.message)
    });
  }

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

    this.error.set(null);
    this.apiService.post('backup', this.addForm.value).subscribe({
      next: () => {
        this.closeAddModal();
        this.loadBackups();
      },
      error: (error) => this.error.set(error.message)
    });
  }
}
