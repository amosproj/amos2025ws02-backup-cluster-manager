import {Component, OnInit, signal} from '@angular/core';
import {NodeFilterParams, NodesService} from './nodes.service';
import {ApiService} from '../../core/services/api.service';
import {AsyncPipe} from '@angular/common';
import {DataTable} from '../../shared/components/data-table/data-table';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';

@Component({
  selector: 'app-nodes',
  imports: [
    AsyncPipe,
    DataTable
  ],
  templateUrl: './nodes.html',
  styleUrl: './nodes.css',
})
export class Nodes implements OnInit {
  nodes = signal<any[]>([]);
  tableColumns = signal([
    {field: 'id', header: 'ID'},
    {field: 'name', header: 'Name'},
    {field: 'status', header: 'Status'},
    {field: 'createdAt', header: 'Created At'},
  ]);

  tableFilters = signal([
    {
      label: 'Active',
      active: false,
    }
  ]);
  
  error = signal<string | null>(null);
  loading$;

  private currentParams: NodeFilterParams & { search?: string, sortBy?: string, sortOrder?: 'asc' | 'desc' } = { active: false };
  private searchSubject = new Subject<string>();

  constructor(
    private nodesService: NodesService,
    private apiService: ApiService
  ) {
    this.loading$ = this.apiService.loading$;
  }

  ngOnInit() {
    this.loadNodes();
    
    // Debounce search: wait 300ms after user stops typing
    this.searchSubject.pipe(
      debounceTime(1000),
      distinctUntilChanged()
    ).subscribe(search => {
      this.currentParams = { ...this.currentParams, search };
      this.loadNodes();
    });
  }

  onSearchChange(search: string) {
    // Don't call API immediately, push to subject instead
    this.searchSubject.next(search);
  }

  onFiltersChange(filters: any[]) {    
    const isActive = filters.some(f => f.active);
    this.currentParams = { ...this.currentParams, active: isActive };
    this.loadNodes();
  }

  onSortChange(sort: { sortBy: string, sortOrder: 'asc' | 'desc' }) {
    this.currentParams = { 
      ...this.currentParams, 
      sortBy: sort.sortBy, 
      sortOrder: sort.sortOrder 
    };
    this.loadNodes();
  }

  loadNodes() {
    this.error.set(null);
    this.nodesService.getFilteredNodes(this.currentParams).subscribe({
      next: (data) => this.nodes.set(data),
      error: (error) => this.error.set(error.message)
    });
  }
  
  ngOnDestroy() {
    this.searchSubject.complete();
  }
}