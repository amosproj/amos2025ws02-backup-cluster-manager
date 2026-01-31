import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/SortTypes';
import {NodeDTO} from '../clients/clients.service';



/** DTO for creating a backup (client, task, size, node). */
export interface BackupDTO {
  clientId: number;
  taskId: number;
  sizeBytes: number;
  nodeDTO: NodeDTO;
}

/**
 * Service for backups API: list (paginated), create, delete.
 */
@Injectable({
  providedIn: 'root',
})
export class BackupsService {
  constructor(private apiService: ApiService) {
  }

  /**
   * Fetches a page of backups with filters, search, and sort.
   * @param page - Page number
   * @param itemsPerPage - Page size
   * @param filters - Filter string
   * @param search - Search string
   * @param sortBy - Sort field
   * @param sortOrder - Sort direction
   * @returns Observable of paginated response
   */
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

  /** Deletes a backup by id on the given node. */
  deleteBackup(backupId: number, nodeAddress: string): Observable<any> {
    return this.apiService.delete<any>(`backups/${backupId}`, {
      params: { nodeAddress }
    });
  }

}
