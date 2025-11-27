import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';


export interface ClientDTO {
   id: number;
   nameOrIp: string;
   enabled: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ClientsService {
  constructor(private apiService: ApiService) {
  }

  getClients(): Observable<any[]> {
    return this.apiService.get<any[]>('clients');
  }
}
