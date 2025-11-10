import {Component, Input, OnChanges, signal, SimpleChanges} from '@angular/core';

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

  tableColumns = signal(this.columns);
  tableData = signal(this.data);
  tableSearchColumns = signal(this.searchColumns);
  tableDataLoading = signal(this.loading);
  tableFilters = signal(this.filters);

  private currentSearchQuery: string = '';

  // Lifecycle hook to detect changes in input properties
  ngOnChanges(changes: SimpleChanges) {
    if (changes['data']) {
      this.tableData.set(this.data);
    }
    if (changes['columns']) {
      this.tableColumns.set(this.columns);
    }
    if (changes['searchColumns']) {
      this.tableSearchColumns.set(this.searchColumns);
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

  // Handle Search triggered by input event
  handleSearch(event: Event) {
    const target = event.target as HTMLInputElement;
    if (target && this.data) {
      this.currentSearchQuery = target.value.toLowerCase();
      this.applySearchAndFilters();
    }
  }

  // Handle Filter logic triggered by clicking on filter buttons
  toggleFilter(filter: any) {
    filter.active = !filter.active;
    this.tableFilters.set([...this.tableFilters()]);
    this.applySearchAndFilters();
  }

  // Apply both search and filters to the data
  applySearchAndFilters() {
    let filteredData = [...this.data];

    // 1. Apply Search
    if (this.currentSearchQuery) {
      filteredData = filteredData.filter(item =>
        this.searchColumns.some(column =>
          item[column] && item[column].toString().toLowerCase().includes(this.currentSearchQuery)));
    }

    // 2. Apply Filters
    const activeFilters = this.tableFilters().filter(f => f.active);
    if (activeFilters.length > 0) {
      filteredData = filteredData.filter(item => {
        return activeFilters.every(filter => filter.filterFn(item))
      })
    }
    this.tableData.set(filteredData);
  }

}
