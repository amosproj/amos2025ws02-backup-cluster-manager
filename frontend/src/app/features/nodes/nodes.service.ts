import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {SortOrder} from '../../shared/types/FilterTypes';


@Injectable({
  providedIn: 'root',
})
export class NodesService {
  constructor(private apiService: ApiService) {
  }

  getNodes(page: number = 1, itemsPerPage: number = 15, search: string = "", orderBy: string = "", sortOrder: SortOrder = SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      search: search.toString(),
      orderBy: orderBy.toString(),
      sortOrder: sortOrder.toString(),
    }
    return this.apiService.get<PaginatedResponse>('nodes', {params});
  }
}

/*
old version:
import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import { HttpParams } from '@angular/common/http';

export interface NodeFilterParams {
  active?: boolean;
  search?: string;
}

@Injectable({
  providedIn: 'root',
})
export class NodesService {
  constructor(private apiService: ApiService){}

  getNodes(): Observable<any[]> {
    return this.apiService.get<any[]>('nodes');
  }

  getFilteredNodes(params: NodeFilterParams): Observable<any> {
    let httpParams = new HttpParams();

    Object.keys(params).forEach(key => {
      const value = params[key as keyof NodeFilterParams];
      if (value !== null && value !== undefined && value !== '') {
        httpParams = httpParams.append(key, String(value));
      }
    });
    console.log('Fetching nodes with params:', httpParams.toString());
    return this.apiService.get<any>('nodes', { params: httpParams });
  }
}
 */
