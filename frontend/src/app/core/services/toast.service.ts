import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ToastMessage, ToastTypeEnum } from '../../shared/types/toast';

@Injectable({
  providedIn: 'root' // ensures the service is global
})
export class ToastService {
  private toastSubject = new BehaviorSubject<ToastMessage | null>(null);
  toast$ = this.toastSubject.asObservable();

  show(text: string, type: ToastMessage['type'] = ToastTypeEnum.INFO, duration = 3000) {
    this.toastSubject.next({ text, type, duration });
  }
}
