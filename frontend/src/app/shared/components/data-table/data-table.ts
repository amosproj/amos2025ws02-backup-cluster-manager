import {Component, Input, Output, OnChanges, signal, SimpleChanges, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-data-table',
  imports: [],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})
export class DataTable implements OnChanges {
  @Input() data: any[] = [];
  @Input() columns: { field: string, header: string }[] = [];
  @Input() searchColumns: string[] = [];
  @Input() filters: any[] = [];
  @Input() loading: boolean | null = false;

  @Output() searchChange = new EventEmitter<string>();
  @Output() filtersChange = new EventEmitter<any[]>();
  @Output() sortChange = new EventEmitter<{ sortBy: string, sortOrder: 'asc' | 'desc' }>();


  tableColumns = signal(this.columns);
  tableData = signal(this.data);
  tableDataLoading = signal(this.loading);
  tableFilters = signal(this.filters);

  currentSortBy = signal<string | null>(null);
  currentSortOrder = signal<'asc' | 'desc'>('asc');

  // Lifecycle hook to detect changes in input properties
  ngOnChanges(changes: SimpleChanges) {
    if (changes['data']) {
      this.tableData.set(this.data);
    }
    if (changes['columns']) {
      this.tableColumns.set(this.columns);
    }
    if (changes['filters']) {
      this.tableFilters.set(this.filters);
    }
    if (changes['loading']) {
      this.tableDataLoading.set(this.loading);
    }
  }

  getColumnValue(item: any, field: string): any {
    return item[field];
  }

  // Handle Search triggered by input event - emit to parent
  handleSearch(event: Event) {
    const target = event.target as HTMLInputElement;
    const searchQuery = target?.value?.trim() || '';
    this.searchChange.emit(searchQuery);
  }

  // Handle Filter logic triggered by clicking on filter buttons
  toggleFilter(filter: any) {
    filter.active = !filter.active;
    this.tableFilters.set([...this.tableFilters()]);
    this.filtersChange.emit(this.tableFilters());
  }

  handleSort(field: string) {
    let newOrder: 'asc' | 'desc' = 'asc';
    
    // If clicking the same column, toggle the order
    if (this.currentSortBy() === field) {
      newOrder = this.currentSortOrder() === 'asc' ? 'desc' : 'asc';
    }
    
    this.currentSortBy.set(field);
    this.currentSortOrder.set(newOrder);
    
    this.sortChange.emit({ sortBy: field, sortOrder: newOrder });
  }

  getSortIcon(field: string): string {
    if (this.currentSortBy() !== field) {
      return ''; // No icon for unsorted columns
    }
    return this.currentSortOrder() === 'asc' ? '↑' : '↓';
  }

}
