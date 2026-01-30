import { Injectable } from '@angular/core';
import { BehaviorSubject, interval } from 'rxjs';
import { filter, mapTo, withLatestFrom } from 'rxjs/operators';

/**
 * Auto-refresh for data tables: emits on an interval when enabled; can be toggled from the UI.
 */
@Injectable({ providedIn: 'root' })
export class AutoRefreshService {
  private enabledSubject = new BehaviorSubject<boolean>(true);
  /** Observable of whether auto-refresh is enabled. */
  readonly isEnabled$ = this.enabledSubject.asObservable();

  /** Whether auto-refresh is currently enabled. */
  get isEnabled(): boolean {
    return this.enabledSubject.value;
  }

  /** Emits every 5 seconds when enabled; used to trigger table refresh. */
  readonly refresh$ = interval(5000).pipe(
    withLatestFrom(this.isEnabled$),
    filter(([, enabled]) => enabled),
    mapTo(void 0)
  );

  /** Toggles auto-refresh on/off. */
  toggle(): void {
    this.enabledSubject.next(!this.enabledSubject.value);
  }
}
