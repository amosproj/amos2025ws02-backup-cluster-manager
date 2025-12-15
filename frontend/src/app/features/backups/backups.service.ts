import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/SortTypes';
import {NodeDTO} from '../clients/clients.service';



export interface BackupDTO {
  clientId: number;
  taskId: number;
  sizeBytes: number;
  nodeDTO: NodeDTO;
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

  createBackup(request: BackupDTO): Observable<any> {
    return this.apiService.post<any>('backups', request);
  }

  deleteBackup(backupId: number, nodeAddress: string): Observable<any> {
    return this.apiService.delete<any>(`backups/${backupId}`, {
      params: { nodeAddress }
    });
  }

}
