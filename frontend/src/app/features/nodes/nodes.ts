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
import {ToastService} from '../../core/services/toast.service';
import {ToastTypeEnum} from '../../shared/types/toast';
import { UsersModal } from '../../shared/components/users-modal/users-modal';

export interface NodeItem {
  id: string;
  name: string;
  status: string;
  address: string;
  mode: string;
  createdAt: string;
}

@Component({
  selector: 'app-nodes',
  imports: [
    DataTable, UsersModal
  ],
  templateUrl: './nodes.html',
  styleUrl: './nodes.css',
})
export class Nodes {
  @ViewChild('nodeTable') dataTable?: DataTable;
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
    },
    {
      label: 'Shutting_down',
      active: false,
    },
    {
      label: 'Restarting',
      active: false,
    }
  ]);

  selectedNode: NodeItem | null = null;
  showActionModal = signal(false);
  actionLoading = signal(false);

  constructor(
    private nodesService: NodesService,
    public authService: AuthService,
    public toast: ToastService
  ) {
  }

  isAddNodeModalOpen = false;
  refreshTrigger = signal(0);
  modalMode: 'node' = 'node';
  openAddNodeModal(mode: 'node' ) {
    this.modalMode = mode;
    this.isAddNodeModalOpen = true;
  }

  fetchNodes = (page: number, itemsPerPage: number, filters: string, search: string, sortBy: string, sortOrder: SortOrder) => {
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
        this.dataTable?.loadData();
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
          this.toast.show('Node deleted successfully!', ToastTypeEnum.SUCCESS);
        },
        error: (error) => {
          this.toast.show('Error deleting node!', ToastTypeEnum.ERROR);
          console.error('Error deleting node:', error);
        }
      });
    });
  }

  onModalClosed() {
    this.isAddNodeModalOpen = false;
    this.refreshTrigger.update(value => value + 1);
  }


  canControlNodes(): boolean {
    return this.authService.hasPermission(UserPermissionsEnum.NodeControl);
  }

  canDeleteNodes(): boolean {
    return this.authService.hasPermission(UserPermissionsEnum.NodeDelete);
  }

  onNodeAction(node: NodeItem, action: 'shutdown' | 'restart') {
    this.selectedNode = node;

    if (action === 'shutdown') {
      this.shutdownNode(node);
    } else if (action === 'restart') {
      this.restartNode(node);
    }
  }

  shutdownNode(node: NodeItem) {
    if (node.mode === 'cluster_manager') {
      this.toast.show("Cannot shutdown cluster manager", ToastTypeEnum.ERROR);
      return;
    }

    this.actionLoading.set(true);
    this.nodesService.shutdownNode(node.id).subscribe({
      next: (response) => {
        this.toast.show(response.message || `Shutdown command sent to ${node.name}`, ToastTypeEnum.SUCCESS);
        this.actionLoading.set(false);
        this.refreshData();
      },
      error: (err) => {
        this.toast.show(`Failed to shutdown node: ${err.message}`, ToastTypeEnum.ERROR);
        this.actionLoading.set(false);
      }
    });
  }

  restartNode(node: NodeItem) {
    if (node.mode === 'cluster_manager') {
      this.toast.show(`Cannot restart cluster manager`, ToastTypeEnum.ERROR);
      return;
    }

    this.actionLoading.set(true);
    this.nodesService.restartNode(node.id).subscribe({
      next: (response) => {
        this.toast.show(response.message || `Restart command sent to ${node.name}`, ToastTypeEnum.SUCCESS);
        this.actionLoading.set(false);
        this.refreshData();
      },
      error: (err) => {
        this.toast.show(`Failed to restart node: ${err.message}`, ToastTypeEnum.ERROR);
        this.actionLoading.set(false);
      }
    });
  }

  refreshData() {
    this.dataTable?.loadData();
  }
}
