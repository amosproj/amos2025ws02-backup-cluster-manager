import {Component, OnInit, signal} from '@angular/core';
import {TasksService } from './tasks.service';
import {ClientsService} from '../clients/clients';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

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

  showAddModal = signal(false);
  addForm!: FormGroup;


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
      enabled: ['', Validators.required],
      interval: ['', Validators.required],
    });
  }

  openAddModal() {
    this.addForm.reset();
    this.showAddModal.set(true);
  }

  closeAddModal() {
    this.showAddModal.set(false);
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

  submitTask() {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }

    this.tasksService.createTask(this.addForm.value).subscribe({
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
