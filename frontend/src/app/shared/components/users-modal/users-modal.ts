import { Component, Input, Output, EventEmitter, OnChanges, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { UsersService } from '../../../features/users/users.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

type Group = { id: number; name: string; enabled?: boolean };

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
    password: string;
    status: string;
    role: string | number; // can be group name or id depending on backend usage
  } = {
    name: '',
    password: '',
    status: 'enabled',
    role: '',
  };
  @Output() submitted = new EventEmitter<any>();

  groups: Group[] = [];
  suggestions: string[] = [];
  private nameInput$ = new Subject<string>();

  constructor(private api: ApiService, private usersService: UsersService) {}

  ngOnInit() {
    this.loadGroups();
    // setup debounced search stream
    this.nameInput$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap(term => this.usersService.searchUsernames(term))
      )
      .subscribe(results => this.suggestions = results);
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
      this.formData.password = '';
      this.formData.role = this.user.role || '';}
  }

  onNameInput(value: string) {
    this.formData.name = value;
    if (value && value.length >= 2) {
      this.nameInput$.next(value.trim());
    } else {
      this.suggestions = [];
    }
  }

  pickSuggestion(name: string) {
    this.formData.name = name;
    this.suggestions = [];
  }

  onSubmit() {
    // send the form data to the parent component or service
    this.submitted.emit({ ...this.formData });
    this.close();
  }

  close() {
    this.closed.emit();
  }
}