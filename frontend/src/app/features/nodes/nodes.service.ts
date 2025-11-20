import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/SortTypes';


@Injectable({
  providedIn: 'root',
})
export class NodesService {
  constructor(private apiService: ApiService) {
  }

  getNodes(page: number = 1, itemsPerPage: number = 15, active: boolean = false, search: string = "", sortBy: string = "", sortOrder: SortOrder = SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      active: active.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
    }
    return this.apiService.get<PaginatedResponse>('nodes', {params});
  }
}
