import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import { HttpParams } from '@angular/common/http';

export interface NodeFilterParams {
  active?: boolean;
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
      if (value !== null && value !== undefined) {
        httpParams = httpParams.append(key, String(value));
      }
    });

    return this.apiService.get<any>('nodes', { params: httpParams });
  }
}