import {Component, signal} from '@angular/core';
import {NodesService} from './nodes.service';
import {DataTable} from '../../shared/components/data-table/data-table';
import {SortOrder} from '../../shared/types/SortTypes';
import {map} from 'rxjs';
import {formatDateFields} from '../../shared/utils/date_utils';
import {PaginatedResponse} from '../../shared/types/PaginationTypes';
import {AuthService} from '../../core/services/auth.service';
import UserPermissionsEnum from '../../shared/types/Permissions';
import {ToastService} from '../../core/services/toast.service';
import {ToastTypeEnum} from '../../shared/types/toast';

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
    DataTable
  ],
  templateUrl: './nodes.html',
  styleUrl: './nodes.css',
})
export class Nodes {
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'address', header: 'Address'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  // Filter for all node statuses
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

  private dataTableRef: DataTable | null = null;

  constructor(
    private nodesService: NodesService,
    public authService: AuthService,
    public toast: ToastService
  ) {}

  fetchNodes = (page: number, itemsPerPage: number, filters: string, search:string, sortBy: string, sortOrder:SortOrder) => {
    return this.nodesService
      .getNodes(page, itemsPerPage, filters, search, sortBy, sortOrder)
      .pipe(map((result: PaginatedResponse) =>
        formatDateFields(result, ['createdAt'])
      ));
  }

  canControlNodes(): boolean {
    return this.authService.hasPermission(UserPermissionsEnum.NodeControl);
  }

  canDeleteNodes(): boolean {
    return this.authService.hasPermission(UserPermissionsEnum.NodeDelete);
  }

  onNodeAction(node: NodeItem, action: 'shutdown' | 'restart' | 'remove') {
    this.selectedNode = node;

    if (action === 'shutdown') {
      this.shutdownNode(node);
    } else if (action === 'restart') {
      this.restartNode(node);
    } else if (action === 'remove') {
      this.removeNode(node);
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
        this.toast.show(response.message|| `Restart command sent to ${node.name}`, ToastTypeEnum.SUCCESS);
        this.actionLoading.set(false);
        this.refreshData();
      },
      error: (err) => {
        this.toast.show(`Failed to restart node: ${err.message}`, ToastTypeEnum.ERROR);
        this.actionLoading.set(false);
      }
    });
  }

  removeNode(node: NodeItem) {
    if (node.mode === 'cluster_manager') {
      this.toast.show("Cannot remove cluster manager", ToastTypeEnum.ERROR);
      return;
    }

    this.actionLoading.set(true);
    this.nodesService.removeNode(node.id).subscribe({
      next: (response) => {
        this.toast.show(response.message || `Node ${node.name} removed from cluster`, ToastTypeEnum.SUCCESS);
        this.actionLoading.set(false);
        this.refreshData();
      },
      error: (err) => {
        this.toast.show(`Failed to remove node: ${err.message}`, ToastTypeEnum.ERROR);
        this.actionLoading.set(false);
      }
    });
  }

  setDataTableRef(ref: DataTable) {
    this.dataTableRef = ref;
  }

  refreshData() {
    if (this.dataTableRef) {
      this.dataTableRef.loadData();
    }
  }

  protected readonly UserPermissionsEnum = UserPermissionsEnum;
}
