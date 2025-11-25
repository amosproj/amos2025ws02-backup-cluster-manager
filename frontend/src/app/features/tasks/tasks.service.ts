import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';


export interface TaskDTO {
  id: number;
  name: string;
  clientId: number;
  source: string;
  enabled: boolean;
  interval: string;
}

@Injectable({
  providedIn: 'root',
})
export class TasksService {
  constructor(private apiService: ApiService) {
  }

  getTasks(): Observable<any[]> {
    return this.apiService.get<any[]>('tasks');
  }

  createTask(request: TaskDTO): Observable<any> {
    return this.apiService.post<any>('task', request);
  }
}
