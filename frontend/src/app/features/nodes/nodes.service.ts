import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class NodesService {
  constructor(private apiService: ApiService){}

  getNodes():Observable<any[]>{
    return this.apiService.get<any[]>('nodes');
  }
}
