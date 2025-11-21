import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class TasksService {
  constructor(private apiService: ApiService){}

  getTasks():Observable<any[]>{
    return this.apiService.get<any[]>('tasks');
  }
}
