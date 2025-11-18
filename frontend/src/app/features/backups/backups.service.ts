import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';

@Injectable({
  providedIn: 'root',
})
export class BackupsService {
  constructor(private apiService: ApiService) {
  }

  getBackups(page: number = 1, itemsPerPage: number = 15): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
    }
    return this.apiService.get<PaginatedResponse>('backups', params);
  }
}
