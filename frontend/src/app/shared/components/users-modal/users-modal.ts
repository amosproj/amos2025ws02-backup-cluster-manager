import { Component, Input, Output, EventEmitter, OnChanges, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

type Group = { id: number; name: string; enabled?: boolean };

@Component({
  selector: 'app-add-users-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users-modal.html',
})
export class UsersModal implements OnChanges, OnInit {
  @Input() open: boolean = false;
  @Input() mode: 'create' | 'edit' | 'delete' = 'create';
  @Input() user: any | null = null;
  @Output() closed = new EventEmitter<void>();

  formData: {
    name: string;
    passwordHash: string;
    status: string;
    createdAt: string | null;
    updatedAt?: string | null;
    role: string | number; // can be group name or id depending on backend usage
  } = {
    name: '',
    passwordHash: '',
    status: 'enabled',
    createdAt: null,
    updatedAt: null,
    role: '',
  };
  @Output() submitted = new EventEmitter<any>();

  groups: Group[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadGroups();
  }

  private loadGroups() {
    this.api.get<Group[]>('groups').subscribe({
      next: (groups) => {
        this.groups = groups || [];
        // If creating and no role selected, preselect first enabled group if exists
        if (this.mode === 'create' && !this.formData.role && this.groups.length > 0) {
          const first = this.groups.find(g => g.enabled !== false) || this.groups[0];
          this.formData.role = first.id ?? first.name;
        }
      },
      error: (err) => {
        console.error('Failed to load groups', err);
      }
    });
  }

  ngOnChanges() {
     if (this.mode === 'edit' && this.user) {
      this.formData.name = this.user.name;
      this.formData.status = this.user.status;
      this.formData.passwordHash = '';
      this.formData.createdAt = this.user.createdAt || this.formData.createdAt;
    }
    if (this.mode === 'create') {
      // reset timestamps for fresh create
      this.formData.createdAt = null;
      this.formData.updatedAt = null;
    }
  }

  onSubmit() {
    // send the form data to the parent component or service
   const now = new Date().toISOString();
    if (this.mode === 'create') {
      this.formData.createdAt = now;
      // ensure updatedAt not sent for create
      delete this.formData.updatedAt;
    } else if (this.mode === 'edit') {
      this.formData.updatedAt = now;
      // keep existing createdAt if present
      if (!this.formData.createdAt && this.user?.createdAt) {
        this.formData.createdAt = this.user.createdAt;
      }
    }
    this.submitted.emit({ ...this.formData });
    this.close();
  }

  close() {
    this.closed.emit();
  }
}