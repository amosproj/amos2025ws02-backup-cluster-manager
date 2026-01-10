import { Component, Input, Output, EventEmitter, OnChanges, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

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
    id?: number;
    name: string;
    passwordHash: string;
    enabled: boolean; // true enabled, false disabled
    groupId?: number;
  } = {
    id: undefined,
    name: '',
    passwordHash: '',
    enabled: true
  };

  groups: Group[] = [];
  chooseUsers: User[] = [];
  loadingUsers = false;
  usersSearchPerformed = false; // to differentiate between 'not searched yet' and 'no results'
  selectedSuggestionIndex: number = -1; // keyboard navigation index

  private nameInput$ = new Subject<string>();

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadGroups();

    this.nameInput$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap(term => this.api.get<User[]>(`users/search/${term}`))
      )
      .subscribe({
        next: results => {
          this.chooseUsers = results;
          this.loadingUsers = false;
          this.usersSearchPerformed = true;
          this.selectedSuggestionIndex = -1; // reset index after new results
        },
        error: err => {
          console.error('User search failed', err);
          this.loadingUsers = false;
          this.usersSearchPerformed = true;
          this.selectedSuggestionIndex = -1;
        }
      });
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

  trackByUserName(index: number, user: User) { return user.name; }

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
    console.log("payload", payload);
    this.api.put(`users/${this.user.id}`, payload).subscribe({
      next: () => {
        this.close();
      },
      error: (err: any) => {
        console.error('Failed to create user', err);
      }
    });    
  }

  onDeleteSubmit(id?: number) {
      const userId = id
      this.api.delete(`users/${userId}`).subscribe({
        next: () => {
          this.close();
        },
        error: (err: any) => {
          console.error('Failed to delete user', err);
        }
      }); 
  }

  close() {
    this.closed.emit();
  }

  onNameKeyDown(event: KeyboardEvent) {
    // Only handle when suggestions list is visible
    const hasSuggestions = this.chooseUsers && this.chooseUsers.length > 0 && !this.loadingUsers;
    if (!hasSuggestions && event.key !== 'Escape') { return; }

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