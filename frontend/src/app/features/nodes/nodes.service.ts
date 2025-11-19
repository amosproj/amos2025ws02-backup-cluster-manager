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