import {Component, signal, ViewChild} from '@angular/core';
import {NodesService} from './nodes.service';

import {DataTable} from '../../shared/components/data-table/data-table';
import {SortOrder} from '../../shared/types/SortTypes';
import {map} from 'rxjs';
import {formatDateFields} from '../../shared/utils/date_utils';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {AuthService} from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';
import {NodeDTO} from '../clients/clients.service';

@Component({
  selector: 'app-nodes',
  imports: [
    DataTable
  ],
  templateUrl: './nodes.html',
  styleUrl: './nodes.css',
})
export class Nodes {
  @ViewChild(DataTable) dataTable!: DataTable;

  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'address', header: 'Address'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  // Example filter: filter backups by 'active' status
  tableFilters = signal([
    {
      label: 'Active',
      active: false,
    },
    {
      label: 'Inactive',
      active: false,
    }
  ]);

  constructor(
    private nodesService: NodesService,
    public authService: AuthService
  ) {}

  fetchNodes = (page: number, itemsPerPage: number, filters: string, search:string, sortBy: string, sortOrder:SortOrder) => {
    return this.nodesService
      .getNodes(page, itemsPerPage, filters, search, sortBy, sortOrder)
      .pipe(map((result: PaginatedResponse) =>
        formatDateFields(result, ['createdAt'])
      ));
  }
  protected readonly UserPermissionsEnum = UserPermissionsEnum;

  onUpdate(item: NodeDTO): void {
    if (!item) return;

    item.createdAt = "";

      this.nodesService.updateNode(item).subscribe({
        next: () => {
          this.dataTable.loadData();
        },
        error: (error) => {
          console.error('Error updating node:', error);
        }
      });
  }

  onDeleteSelection(rows: any[]): void {
    if (!rows.length) return;

    let completed = 0;

    rows.forEach(row => {
      this.nodesService.deleteNode(row.id).subscribe({
        next: () => {
          completed++;

          if (completed === rows.length && this.dataTable) {
            this.dataTable.loadData();
          }
        },
        error: (error) => {
          console.error('Error deleting node:', error);
        }
      });
    });
  }

}
