import { Component, OnInit, signal } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { BackupsService } from './backups.service';
import { AsyncPipe } from '@angular/common';
import { DataTable } from '../../shared/components/data-table/data-table';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';

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
    { field: 'id', header: 'ID' },
    { field: 'clientId', header: 'Client ID' },
    { field: 'taskId', header: 'Task ID' },
    { field: 'sizeBytes', header: 'Size (Bytes)' },
    { field: 'status', header: 'Status' },
    { field: 'startTime', header: 'Start Time' }
  ]);

  // Search
  tableSearchColumns = signal(['clientId', 'taskId', 'status', 'id']);

  //  filter
  tableFilters = signal([
    {
      label: 'Active',
      filterFn: (item: any) => item.status?.toLowerCase() === "active",
      active: false,
    }
  ]);

  error = signal<string | null>(null);
  loading$;

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
    console.log('Submitting backup:', this.addForm.value);

    this.backupsService.createBackup(this.addForm.value).subscribe({
      next: (response) => {
        console.log('Backup created:', response);
        this.closeAddModal();
        this.loadBackups();
      },
      error: (error) => {
        console.error('Error creating backup:', error);
        this.error.set(error.message);
      }
    });
  }
}
