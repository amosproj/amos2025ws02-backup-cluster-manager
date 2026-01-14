import {Component, signal, ViewChild} from '@angular/core';
import {DataTable} from '../../shared/components/data-table/data-table';
import UserPermissionsEnum from '../../shared/types/Permissions';
import {SortOrder} from '../../shared/types/SortTypes';
import {map} from 'rxjs';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {formatDateFields} from '../../shared/utils/date_utils';
import {AuthService} from '../../core/services/auth.service';
import {ToastService} from '../../core/services/toast.service';
import {PermissionsService} from './permissions.service';

@Component({
  selector: 'app-permissions',
  imports: [
    DataTable
  ],
  templateUrl: './permissions.html',
  styleUrl: './permissions.css',
})
export class Permissions {

  constructor(
    private permissionsService: PermissionsService,
    public authService: AuthService,
    public toast: ToastService
  ) {
  }

  @ViewChild('nodeTable') dataTable?: DataTable;
  tableColumns = signal([
    {field: 'role', header: 'Role'},
    {field: 'permissions', header: 'Permissions'},
  ]);

  // Example filter: filter backups by 'active' status
  tableFilters = signal([
  ]);

  protected readonly UserPermissionsEnum = UserPermissionsEnum;

  fetchNodes = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder: SortOrder) => {
    return this.permissionsService
      .getPermissions(page, itemsPerPage, filters, search, sortBy, sortOrder);
  }

}
