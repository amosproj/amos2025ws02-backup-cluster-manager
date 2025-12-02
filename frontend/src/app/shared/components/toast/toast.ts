import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ToastService} from '../../../core/services/toast.service';
import {ToastTypeEnum} from '../../types/toast';

@Component({
  selector: 'app-toast',
  imports: [CommonModule],
  templateUrl: './toast.html',
  styleUrl: './toast.css',
})
export class Toast implements OnInit {
  message: string | null = null;
  type: ToastTypeEnum = ToastTypeEnum.INFO;
  isVisible = false;
  private hideTimeout?: number;

  constructor(private toastService: ToastService) {
  }

  ngOnInit() {
    this.toastService.toast$.subscribe(toast => {
      if (!toast) return;

      // Clear existing timeout
      if (this.hideTimeout) {
        clearTimeout(this.hideTimeout);
      }

      this.message = toast.text;
      this.type = toast.type || ToastTypeEnum.INFO;
      this.isVisible = true;

      // Auto-hide after duration with slide-out animation
      this.hideTimeout = window.setTimeout(() => {
        this.isVisible = false;
      }, toast.duration || 3000);
    });
  }

  readonly ToastTypeEnum = ToastTypeEnum;
}
