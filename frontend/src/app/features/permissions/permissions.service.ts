import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/SortTypes';

@Injectable({
  providedIn: 'root',
})
export class PermissionsService {
  constructor(private apiService: ApiService) {
  }

  getPermissions(page: number = 1, itemsPerPage: number = 15, filters: string = "", search: string = "", sortBy: string = "", sortOrder: SortOrder = SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
      filters: filters.toString(),
    }
    return this.apiService.get<PaginatedResponse>('permissions', {params});
  }
}
