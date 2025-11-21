import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';



export interface BackupRequest {
  clientId: number;
  taskId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class BackupsService {
  constructor(private apiService: ApiService){}

  getBackups(): Observable<any[]> {
    return this.apiService.get<any[]>('backups');
  }

  createBackup(request: BackupRequest): Observable<any> {
    return this.apiService.post<any>('backups', request);
  }

}
