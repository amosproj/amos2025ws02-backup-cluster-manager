import {ApiService} from '../../core/services/api.service';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';

/**
 * Service for users API: paginated list with filters, search, sort.
 */
@Injectable({
  providedIn: 'root',
})
export class UsersService {
  constructor(private apiService:ApiService){}

  /**
   * Fetches a page of users.
   * @param page - Page number
   * @param itemsPerPage - Page size
   * @param filters - Filter string
   * @param search - Search string
   * @param sortBy - Sort field
   * @param sortOrder - Sort direction
   * @returns Observable of paginated response
   */
  getUsers(page:number=1, itemsPerPage:number=15, filters:string="",search:string="", sortBy:string="", sortOrder:string="ASC"):Observable<PaginatedResponse>{

    const params={
      page:page.toString(),
      itemsPerPage:itemsPerPage.toString(),
      filters:filters.toString(),
      search:search.toString(),
      sortBy:sortBy.toString(),
      sortOrder:sortOrder.toString(),
    }
    return this.apiService.get('users/userlist',{params});
  }
}
