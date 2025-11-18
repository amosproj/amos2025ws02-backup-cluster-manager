import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';


@Injectable({
  providedIn: 'root',
})
export class NodesService {
  constructor(private apiService: ApiService){}

  getNodes(page: number = 1, itemsPerPage: number = 15): Observable<PaginatedResponse>{
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
    }
    return this.apiService.get<PaginatedResponse>('nodes', params);
  }
}
