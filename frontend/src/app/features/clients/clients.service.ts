import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import { PaginatedResponse } from '../../shared/types/PaginationTypes';
import { SortOrder } from '../../shared/types/SortTypes';


/** Client DTO with node and enabled flag. */
export interface ClientDTO {
   nodeDTO: NodeDTO;
   id: number;
   nameOrIp: string;
   nodeId: NodeDTO;
   enabled: boolean;

}

/** Node DTO (id, name, address, status, mode, managed). */
export interface NodeDTO {
  id: string ;
  name : string;
  address : string;
  status : any;
  nodeMode: any;
  createdAt: string;
  isManaged: boolean;
}

/**
 * Service for clients API: paginated list and full list.
 */
@Injectable({
  providedIn: 'root',
})
export class ClientsService {
  constructor(private apiService: ApiService) {
  }

  /**
   * Fetches a page of clients with filters, search, sort.
   * @param page - Page number
   * @param itemsPerPage - Page size
   * @param filters - Filter string
   * @param search - Search string
   * @param sortBy - Sort field
   * @param sortOrder - Sort direction
   * @returns Observable of paginated response
   */
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

  /** Fetches all clients (no pagination). */
  getClientList(): Observable<any[]> {
    return this.apiService.get<any[]>('clientsList');
  }
}
