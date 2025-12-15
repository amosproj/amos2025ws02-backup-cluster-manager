import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import { PaginatedResponse } from '../../shared/types/PaginationTypes';
import { SortOrder } from '../../shared/types/SortTypes';


export interface ClientDTO {
   id: number;
   nameOrIp: string;
  //  nodeId: number;
   enabled: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ClientsService {
  constructor(private apiService: ApiService) {
  }
  getClients(page: number = 1, itemsPerPage: number = 15,filters:string="", search:string="", sortBy:string="", sortOrder:SortOrder=SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      filters: filters.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
    }
    return this.apiService.get<PaginatedResponse>('clients', {params});
  }
  getClientList(): Observable<any[]> {
    return this.apiService.get<any[]>('clients');
  }
}
