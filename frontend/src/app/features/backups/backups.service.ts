import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BackupsService {
  constructor(private apiService: ApiService){}

  getBackups():Observable<any[]>{
    return this.apiService.get<any[]>('backups');
  }
}
