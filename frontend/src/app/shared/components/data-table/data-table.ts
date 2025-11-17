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


  tableColumns = signal(this.columns);
  tableData = signal(this.data);
  tableSearchColumns = signal(this.searchColumns);
  tableDataLoading = signal(this.loading);
  tableFilters = signal(this.filters);

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


}
