import {ApiService} from '../../core/services/api.service';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  constructor(private apiService: ApiService) {
  }

  getUsers(page: number = 1, itemsPerPage: number = 15, filters: string = "", search: string = "", sortBy: string = "", sortOrder: string = "ASC"): Observable<PaginatedResponse> {

    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      filters: filters.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
    }
    return this.apiService.get('userlist', {params});
  }

  deleteUser(userId: number): Observable<void>{
      return this.apiService.delete(`users/${userId}`);
  }
}
