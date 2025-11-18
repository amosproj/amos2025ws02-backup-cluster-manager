import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  constructor(private apiService: ApiService){}

  getUsers(page: number = 1, itemsPerPage: number = 15, search: string = ''): Observable<PaginatedResponse>{
    const params: any = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
    };

    // Only add search parameter if it's not empty
    if (search && search.trim() !== '') {
      params.search = search;
    }

    return this.apiService.get<PaginatedResponse>('users', params);
  }
}
