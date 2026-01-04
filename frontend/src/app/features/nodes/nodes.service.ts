import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/SortTypes';
import {BackupDTO} from '../backups/backups.service';
import {NodeDTO} from '../clients/clients.service';


@Injectable({
  providedIn: 'root',
})
export class NodesService {
  constructor(private apiService: ApiService) {
  }

  getNodes(page: number = 1, itemsPerPage: number = 15, filters: string = "", search: string = "", sortBy: string = "", sortOrder: SortOrder = SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
      filters: filters.toString(),
    }
    return this.apiService.get<PaginatedResponse>('nodes', {params});
  }

  updateNode(node: NodeDTO): Observable<any> {
    return this.apiService.put<any>('node', node);
  }

  deleteNode(nodeId: number): Observable<any> {
    return this.apiService.delete<any>(`node/${nodeId}`);
  }
}
