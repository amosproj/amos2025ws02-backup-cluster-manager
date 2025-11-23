import { Component, Input, Output, EventEmitter, OnChanges, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

type Group = { id: number; name: string; enabled?: boolean };
type User = { id?: number; name: string; passwordHash?: string; enabled?: boolean; createdAt?: string; updatedAt?: string; };


@Component({
  selector: 'app-users-modal',
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
    enabled: boolean; // true enabled, false disabled
    groupId?: number;
  } = {
    name: '',
    passwordHash: '',
    enabled: true
  };

  groups: Group[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadGroups();
  }

  private loadGroups() {
    this.api.get<Group[]>('groups').subscribe({
      next: (groups: Group[]) => {
        this.groups = groups || [];
      },
      error: (err: any) => console.error('Failed to load groups', err)
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
        this.close();
      },
      error: (err: any) => {
        console.error('Failed to create user', err);
      }
    });    
  }
  onEditSubmit() {
    const payload = { ...this.formData };


  }
  onDeleteSubmit() {
    if (this.user) {
      const userId = this.user.id;
    }
  }

  close() {
    this.closed.emit();
  }
}