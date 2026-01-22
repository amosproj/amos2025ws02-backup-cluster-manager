import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {SortOrder} from '../../shared/types/SortTypes';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {NodeDTO} from '../clients/clients.service';


export interface TaskDTO {
  id: number | null;
  name: string;
  clientId: number;
  source: string;
  enabled: boolean;
  interval: "DAILY" | "WEEKLY" | "MONTHLY";
  nodeDTO: NodeDTO;
}

@Injectable({
  providedIn: 'root',
})
export class TasksService {
  constructor(private apiService: ApiService) {
  }

  getTasks(page: number = 1, itemsPerPage: number = 15,
           filters: string = "", search: string = "",
           sortBy: string = "", sortOrder: SortOrder = SortOrder.ASC): Observable<PaginatedResponse> {
    const params = {
      page: page.toString(),
      itemsPerPage: itemsPerPage.toString(),
      filters: filters.toString(),
      search: search.toString(),
      sortBy: sortBy.toString(),
      sortOrder: sortOrder.toString(),
    }
    return this.apiService.get<PaginatedResponse>('tasks', {params});
  }

  getTaskList():Observable<TaskDTO[]>{
    return this.apiService.get<any>('tasks/list');
  }

  createTask(request: TaskDTO): Observable<any> {
    return this.apiService.post<any>('task', request);
  }
}
