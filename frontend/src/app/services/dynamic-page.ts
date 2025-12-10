import { Injectable } from '@angular/core';
import { BehaviorSubject, interval } from 'rxjs';
import { filter, mapTo, withLatestFrom } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AutoRefreshService {
  private enabledSubject = new BehaviorSubject<boolean>(true);
  readonly isEnabled$ = this.enabledSubject.asObservable();

  get isEnabled(): boolean {
    return this.enabledSubject.value;
  }

  // Stream that emits every few seconds *only when enabled*
  readonly refresh$ = interval(5000).pipe(
    withLatestFrom(this.isEnabled$),
    filter(([, enabled]) => enabled),
    mapTo(void 0)
  );

  toggle(): void {
    this.enabledSubject.next(!this.enabledSubject.value);
  }
}
