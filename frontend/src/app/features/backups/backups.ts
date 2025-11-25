import { Component, OnInit, signal } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { BackupsService } from './backups.service';
import { AsyncPipe } from '@angular/common';
import { DataTable } from '../../shared/components/data-table/data-table';
import { SortOrder} from '../../shared/types/SortTypes';
import {ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';

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
  tableColumns = signal([
    { field: 'id', header: 'ID' },
    { field: 'clientId', header: 'Client ID' },
    { field: 'taskId', header: 'Task ID' },
    { field: 'sizeBytes', header: 'Size (Bytes)' },
    { field: 'state', header: 'State' },
    { field: 'startTime', header: 'Start Time' }
  ]);

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

  fetchBackups = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder:SortOrder) => {
    return this.backupsService.getBackups(page, itemsPerPage, filters, search, sortBy, sortOrder);
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

    this.apiService.post('backup', this.addForm.value).subscribe({
      next: () => {
        this.closeAddModal();
      },
    });
    this.backupsService.createBackup(this.addForm.value).subscribe({
      next: (response) => {
        //console.log('Backup created:', response);
        this.closeAddModal();
      },
      error: (error) => {
        console.error('Error creating backup:', error);
      }
    });

  }


}
