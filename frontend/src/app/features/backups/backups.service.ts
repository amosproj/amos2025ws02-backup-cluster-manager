import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/SortTypes';



export interface BackupRequest {
  clientId: number;
  taskId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class BackupsService {
  constructor(private apiService: ApiService) {
  }

  getBackups(page: number = 1, itemsPerPage: number = 15,filters:string="", search:string="", sortBy:string="", sortOrder:SortOrder=SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      filters: filters.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
    }
    return this.apiService.get<PaginatedResponse>('backups', {params});
  }

  createBackup(request: BackupRequest): Observable<any> {
    return this.apiService.post<any>('backups', request);
  }

}
