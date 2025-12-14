import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';


export interface ClientDTO {
   nodeDTO: NodeDTO;
   id: number;
   nameOrIp: string;
   enabled: boolean;
}

export interface NodeDTO {
  id: string ;
  name : string;
  address : string;
  status : any;
  nodeMode: any;
  createdAt: string;
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
