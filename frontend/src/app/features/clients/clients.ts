import { Component, signal, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs/internal/Subscription';
import { DataTable } from '../../shared/components/data-table/data-table';
import { ClientsService } from './clients.service';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';
import { AutoRefreshService } from '../../services/dynamic-page';
import { formatDateFields } from '../../shared/utils/date_utils';
import { map } from 'rxjs/internal/operators/map';
import { SortOrder } from '../../shared/types/SortTypes';
import { ReactiveFormsModule } from '@angular/forms';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-clients',
  imports: [AsyncPipe, DataTable, ReactiveFormsModule],
  templateUrl: './clients.html',
  styleUrl: './clients.css',
})
export class Clients {
  @ViewChild(DataTable) dataTable!: DataTable;
  private refreshSub?: Subscription;

  tableColumns = signal([
    { field: 'id', header: 'Client ID' },
    { field: 'nameOrIp', header: 'Name or IP' },
    { field: 'enabled', header: 'Enabled' },
    { field: 'nodeDTO.name', header: 'Node'},
  ]);

  //  filter
  tableFilters = signal([
    {
      filterFn: (item: any) => item.state?.toLowerCase() === 'completed',
      label: 'Completed',
      active: false,
    },
  ]);
  loading$;

  private refreshIntervalId: any;

  constructor(
    private apiService: ApiService,
    private clientsService: ClientsService,
    public authService: AuthService,
    private autoRefreshService: AutoRefreshService
  ) {
    this.loading$ = this.apiService.loading$;
  }

  fetchClients = (
    page: number,
    itemsPerPage: number,
    filters: string,
    search: string,
    sortBy: string,
    sortOrder: SortOrder
  ) => {
    return this.clientsService
      .getClients(page, itemsPerPage, filters, search, sortBy, sortOrder)
      .pipe(map((result: any) => formatDateFields(result, ['startTime', 'stopTime'])));
  };

  ngOnInit(): void {
    this.refreshSub = this.autoRefreshService.refresh$.subscribe(() => {
      if (this.dataTable) {
        this.dataTable.loadData();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
