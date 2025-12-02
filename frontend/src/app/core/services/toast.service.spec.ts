import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';
import { ToastTypeEnum } from '../../shared/types/toast';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ToastService]
    });
    service = TestBed.inject(ToastService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('toast$ observable', () => {
    it('should initially emit null', (done) => {
      service.toast$.subscribe(toast => {
        expect(toast).toBeNull();
        done();
      });
    });
  });

  describe('show method', () => {
    it('should emit a toast message with text and type', (done) => {
      const testText = 'Test message';
      const testType = ToastTypeEnum.SUCCESS;

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe(testText);
          expect(toast.type).toBe(testType);
          expect(toast.duration).toBe(3000); // default duration
          done();
        }
      });

      service.show(testText, testType);
    });

    it('should use INFO as default type when type is not provided', (done) => {
      const testText = 'Info message';

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe(testText);
          expect(toast.type).toBe(ToastTypeEnum.INFO);
          done();
        }
      });

      service.show(testText);
    });

    it('should use 3000ms as default duration when duration is not provided', (done) => {
      const testText = 'Test message';

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.duration).toBe(3000);
          done();
        }
      });

      service.show(testText, ToastTypeEnum.ERROR);
    });

    it('should accept custom duration', (done) => {
      const testText = 'Custom duration message';
      const customDuration = 5000;

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.duration).toBe(customDuration);
          done();
        }
      });

      service.show(testText, ToastTypeEnum.INFO, customDuration);
    });

    it('should emit SUCCESS toast correctly', (done) => {
      const testText = 'Success message';

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe(testText);
          expect(toast.type).toBe(ToastTypeEnum.SUCCESS);
          done();
        }
      });

      service.show(testText, ToastTypeEnum.SUCCESS);
    });

    it('should emit ERROR toast correctly', (done) => {
      const testText = 'Error message';

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe(testText);
          expect(toast.type).toBe(ToastTypeEnum.ERROR);
          done();
        }
      });

      service.show(testText, ToastTypeEnum.ERROR);
    });

    it('should emit INFO toast correctly', (done) => {
      const testText = 'Info message';

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe(testText);
          expect(toast.type).toBe(ToastTypeEnum.INFO);
          done();
        }
      });

      service.show(testText, ToastTypeEnum.INFO);
    });

    it('should emit multiple messages in sequence', () => {
      const messages: any[] = [];

      service.toast$.subscribe(toast => {
        messages.push(toast);
      });

      service.show('First message', ToastTypeEnum.SUCCESS);
      service.show('Second message', ToastTypeEnum.ERROR);
      service.show('Third message', ToastTypeEnum.INFO);

      // First message is null (initial value)
      expect(messages.length).toBe(4);
      expect(messages[0]).toBeNull();
      expect(messages[1]?.text).toBe('First message');
      expect(messages[1]?.type).toBe(ToastTypeEnum.SUCCESS);
      expect(messages[2]?.text).toBe('Second message');
      expect(messages[2]?.type).toBe(ToastTypeEnum.ERROR);
      expect(messages[3]?.text).toBe('Third message');
      expect(messages[3]?.type).toBe(ToastTypeEnum.INFO);
    });

    it('should handle empty text', (done) => {
      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe('');
          done();
        }
      });

      service.show('', ToastTypeEnum.INFO);
    });

    it('should handle very long text', (done) => {
      const longText = 'A'.repeat(1000);

      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.text).toBe(longText);
          expect(toast.text.length).toBe(1000);
          done();
        }
      });

      service.show(longText, ToastTypeEnum.ERROR);
    });

    it('should handle zero duration', (done) => {
      service.toast$.subscribe(toast => {
        if (toast) {
          expect(toast.duration).toBe(0);
          done();
        }
      });

      service.show('Message', ToastTypeEnum.INFO, 0);
    });
  });
});

