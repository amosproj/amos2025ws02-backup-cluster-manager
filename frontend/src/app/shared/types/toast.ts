export enum ToastTypeEnum {
  SUCCESS = 'success',
  ERROR = 'error',
  INFO = 'info'
}

export interface ToastMessage{
  text: string;
  type?: ToastTypeEnum;
  duration?: number;
}
