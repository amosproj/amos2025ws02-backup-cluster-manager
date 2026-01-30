import {Injectable} from '@angular/core';
import {ApiService} from '../../core/services/api.service';
import {Observable} from 'rxjs';
import {SortOrder} from '../../shared/types/SortTypes';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {NodeDTO} from '../clients/clients.service';


/** Task DTO with node and interval. */
export interface TaskDTO {
  id: number | null;
  name: string;
  clientId: number;
  source: string;
  enabled: boolean;
  interval: "DAILY" | "WEEKLY" | "MONTHLY";
  nodeDTO: NodeDTO;
}

/**
 * Service for tasks API: list (paginated and full), create.
 */
@Injectable({
  providedIn: 'root',
})
export class TasksService {
  constructor(private apiService: ApiService) {
  }

  /**
   * Fetches a page of tasks with filters, search, sort.
   * @param page - Page number
   * @param itemsPerPage - Page size
   * @param filters - Filter string
   * @param search - Search string
   * @param sortBy - Sort field
   * @param sortOrder - Sort direction
   * @returns Observable of paginated response
   */
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

  /** Fetches all tasks (no pagination). */
  getTaskList():Observable<TaskDTO[]>{
    return this.apiService.get<any>('tasks/list');
  }

  /** Creates a new task. */
  createTask(request: TaskDTO): Observable<any> {
    return this.apiService.post<any>('task', request);
  }
}
