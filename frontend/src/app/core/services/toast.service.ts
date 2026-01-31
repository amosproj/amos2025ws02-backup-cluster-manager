import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ToastMessage, ToastTypeEnum } from '../../shared/types/toast';

/**
 * Global toast notification service: show messages with type and duration.
 */
@Injectable({
  providedIn: 'root' // ensures the service is global
})
export class ToastService {
  private toastSubject = new BehaviorSubject<ToastMessage | null>(null);
  /** Observable of the current toast message (null when none). */
  toast$ = this.toastSubject.asObservable();

  /**
   * Shows a toast message.
   * @param text - Message text
   * @param type - Toast type (info, success, warning, error)
   * @param duration - Display duration in ms (default 3000)
   */
  show(text: string, type: ToastMessage['type'] = ToastTypeEnum.INFO, duration = 3000) {
    this.toastSubject.next({ text, type, duration });
  }
}
