import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  OnInit,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { NodesService } from '../../../features/nodes/nodes.service';
import { ToastService } from '../../../core/services/toast.service';
import { ToastTypeEnum } from '../../types/toast';
import { ClientsService } from '../../../features/clients/clients.service';
import { TasksService } from '../../../features/tasks/tasks.service';
import { BackupDTO, BackupsService } from '../../../features/backups/backups.service';

type Group = { id: number; name: string; enabled?: boolean };
type User = {
  id?: number;
  name: string;
  passwordHash?: string;
  enabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
};

@Component({
  selector: 'app-users-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './users-modal.html',
})
export class UsersModal implements OnChanges, OnInit {
  @Input() open: boolean = false;
  @Input() mode: 'create' | 'edit' | 'delete' | 'node' | 'backups' | 'tasks' = 'create';
  @Input() user: any | null = null;
  @Output() closed = new EventEmitter<void>();

  clients = signal<any[]>([]);
  tasks = signal<any[]>([]);
  sizeBytes: number = 0;

  intervalOptions = [
    { value: 'DAILY', label: 'Daily' },
    { value: 'WEEKLY', label: 'Weekly' },
    { value: 'MONTHLY', label: 'Monthly' },
  ];

  nodeAddress: string = '';
  formData: {
    id?: number;
    name: string;
    passwordHash: string;
    enabled: boolean; // true enabled, false disabled
    groupId?: number;
  } = {
    id: undefined,
    name: '',
    passwordHash: '',
    enabled: true,
  };

  backupFormData!: FormGroup;
  taskFormData!: FormGroup;
  groups: Group[] = [];
  chooseUsers: User[] = [];
  loadingUsers = false;
  usersSearchPerformed = false; // to differentiate between 'not searched yet' and 'no results'
  selectedSuggestionIndex: number = -1; // keyboard navigation index

  private nameInput$ = new Subject<string>();

  constructor(
    private api: ApiService,
    private nodesService: NodesService,
    private toast: ToastService,
    private backupsService: BackupsService,
    private clientsService: ClientsService,
    private tasksService: TasksService,
    private fb: FormBuilder
  ) {
    this.taskFormData = this.fb.group({
      name: ['', Validators.required],
      source: ['', Validators.required],
      enabled: [true, Validators.required],
      interval: ['', Validators.required],
      clientSelection: [null, Validators.required],
    });
    this.backupFormData = this.fb.group({
      client: [null, Validators.required],
      task: [null, Validators.required],
      sizeBytes: ['', [Validators.required, Validators.min(1)]],
    });
  }

  ngOnInit() {
    if (this.mode === 'create' )
    this.loadGroups();

    if (this.mode === 'backups' || this.mode === 'tasks' ) {
    this.clientsService.getClientList().subscribe({
      next: (clients) => {
        this.clients.set(clients ?? []);
      },
      error: (err) => {
        console.error('Failed to load clients', err);
      },
    });

    this.tasksService.getTaskList().subscribe({
      next: (tasks) => {
        this.tasks.set(tasks ?? []);
      },
      error: (err) => {
        console.error('Failed to load tasks', err);
      },
    });
  }
    this.nameInput$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((term) => this.api.get<User[]>(`users/search/${term}`))
      )
      .subscribe({
        next: (results) => {
          this.chooseUsers = results;
          this.loadingUsers = false;
          this.usersSearchPerformed = true;
          this.selectedSuggestionIndex = -1; // reset index after new results
        },
        error: (err) => {
          console.error('User search failed', err);
          this.loadingUsers = false;
          this.usersSearchPerformed = true;
          this.selectedSuggestionIndex = -1;
        },
      });
  }

  private loadGroups() {
    this.api.get<Group[]>('groups').subscribe({
      next: (groups: Group[]) => {
        this.groups = groups || [];
      },
      error: (err: any) => console.error('Failed to load groups', err),
    });
  }

  ngOnChanges() {
    if (this.mode === 'edit' && this.user) {
      this.formData.name = this.user.name;
      this.formData.enabled = this.user.enabled ? true : false;
      this.formData.passwordHash = '';
      this.formData.groupId = this.user.groupId;
    }
  }

  onCreateSubmit() {
    // Create user payload
    this.user = this.formData;
    console.log(this.user);
    const payload = { ...this.user };
    this.api.post(`users/${this.formData.groupId}`, payload).subscribe({
      next: () => {
        this.toast.show('User created successfully!', ToastTypeEnum.SUCCESS);
        this.close();
      },
      error: (err: any) => {
        this.toast.show('Error creating user!', ToastTypeEnum.ERROR);
        console.error('Failed to create user', err);
      },
    });
  }

  onNameInput(value: string) {
    if (value && value.length >= 1) {
      this.loadingUsers = true;
      this.usersSearchPerformed = false;
      this.nameInput$.next(value.trim());
      this.selectedSuggestionIndex = -1;
    } else {
      this.chooseUsers = [];
      this.loadingUsers = false;
      this.usersSearchPerformed = false;
      this.selectedSuggestionIndex = -1;
    }
  }

  trackByUserName(index: number, user: User) {
    return user.name;
  }

  selectedUser(user: User) {
    this.formData.id = user.id;
    this.formData.name = user.name;
    this.chooseUsers = [];
    this.loadingUsers = false;
    this.usersSearchPerformed = false; // reset suggestions display
    this.selectedSuggestionIndex = -1;
  }

  onEditSubmit(id?: number) {
    this.user = this.formData;
    this.user.id = id;
    const payload = { ...this.user };
    payload.passwordHash = undefined; // only include if set
    console.log('payload', payload);
    this.api.put(`users/${this.user.id}`, payload).subscribe({
      next: () => {
        this.toast.show('User updated successfully!', ToastTypeEnum.SUCCESS);
        this.close();
      },
      error: (err: any) => {
        this.toast.show('Error updating user!', ToastTypeEnum.ERROR);
        console.error('Failed to update user', err);
      },
    });
  }

  onDeleteSubmit(users: Array<User>) {
    for (let i of users) {
      const userId = i.id;
      console.log('Deleting user ID', userId);
      this.api.delete(`users/${userId}`).subscribe({
        next: () => {
          this.toast.show('User deleted successfully!', ToastTypeEnum.SUCCESS);
          this.close();
        },
        error: (err: any) => {
          this.toast.show('Error deleting user!', ToastTypeEnum.ERROR);
          console.error('Failed to delete user', err);
        },
      });
    }
  }

  onAddNode(): void {
    const address = this.nodeAddress.trim();

    this.nodesService.addNode(address).subscribe({
      next: () => {
        this.toast.show('Node added successfully!', ToastTypeEnum.SUCCESS);
        this.close();
      },
      error: (error) => {
        console.error(error);
        this.toast.show('Error adding node!', ToastTypeEnum.ERROR);
      },
    });
  }

  submitAddBackup() {
    const { client, task, sizeBytes } = this.backupFormData.value;
    const payload: BackupDTO = {
      clientId: client.id,
      taskId: task.id,
      sizeBytes: Number(sizeBytes),
      nodeDTO: client.nodeDTO,
    };

    console.log('Backup payload:', payload);

    this.backupsService.createBackup(payload).subscribe({
      next: (response) => {
        //console.log('Backup created:', response);
        this.toast.show('Backup created successfully!', ToastTypeEnum.SUCCESS);
        this.close();
      },
      error: (error) => {
        this.toast.show('Error creating backup!', ToastTypeEnum.ERROR);
        console.error('Error creating backup:', error);
      },
    });
  }

  onAddTask(): void {
    const { clientSelection, ...taskData } = this.taskFormData.value;
    // console.log("Payload:",  {id:null, clientId: client.clientId, name,  source, enabled, interval, node: client.nodeDTO});
    this.tasksService.createTask({ id: null, ...clientSelection, ...taskData }).subscribe({
      next: (response) => {
        this.toast.show('Task created successfully!', ToastTypeEnum.SUCCESS);
        // console.log('Task created:', response);
        this.close();
      },
      error: (error) => {
        this.toast.show('Error creating task!', ToastTypeEnum.ERROR);
        console.error('Error creating task:', error);
      },
    });
  }

  close() {
    this.closed.emit();
  }

  onNameKeyDown(event: KeyboardEvent) {
    // Only handle when suggestions list is visible
    const hasSuggestions = this.chooseUsers && this.chooseUsers.length > 0 && !this.loadingUsers;
    if (!hasSuggestions && event.key !== 'Escape') {
      return;
    }

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.selectedSuggestionIndex = (this.selectedSuggestionIndex + 1) % this.chooseUsers.length;
        this.scrollActiveIntoView();
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.selectedSuggestionIndex =
          this.selectedSuggestionIndex <= 0
            ? this.chooseUsers.length - 1
            : this.selectedSuggestionIndex - 1;
        this.scrollActiveIntoView();
        break;
      case 'Enter':
        if (this.selectedSuggestionIndex >= 0) {
          event.preventDefault();
          this.selectedUser(this.chooseUsers[this.selectedSuggestionIndex]);
        }
        break;
      case 'Tab':
        if (this.selectedSuggestionIndex >= 0) {
          this.selectedUser(this.chooseUsers[this.selectedSuggestionIndex]);
        }
        break;
      case 'Escape':
        if (this.chooseUsers.length) {
          this.chooseUsers = [];
          this.usersSearchPerformed = false;
          this.selectedSuggestionIndex = -1;
        }
        break;
      default:
        return;
    }
  }

  private scrollActiveIntoView() {
    const id = this.getActiveDescendantId();
    if (!id) return;
    setTimeout(() => {
      const el = document.getElementById(id);
      el?.scrollIntoView({ block: 'nearest' });
    });
  }

  getActiveDescendantId(): string | null {
    return this.selectedSuggestionIndex >= 0 ? `user-option-${this.selectedSuggestionIndex}` : null;
  }
}
