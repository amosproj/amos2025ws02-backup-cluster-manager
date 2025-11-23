import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-add-users-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users-modal.html',
})
export class UsersModal implements OnChanges {
  @Input() open: boolean = false;
  @Input() mode: 'create' | 'edit' | 'delete' = 'create';
  @Input() user: any | null = null;
  @Output() closed = new EventEmitter<void>();

  formData = {
    name: '',
    passwordHash: '',
    status: 'enabled'
  };

  ngOnChanges() {
    if (this.mode === 'edit' && this.user) {
      this.formData.name = this.user.name;
      this.formData.status = this.user.status;
      this.formData.passwordHash = '';
    }
  }

  onSubmit() {
    // handle create, update, delete logic
    this.close();
  }

  close() {
    this.closed.emit();
  }
}