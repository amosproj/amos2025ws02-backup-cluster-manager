import {Component, Input, OnChanges, OnInit, signal, SimpleChanges} from '@angular/core';
import {Observable} from 'rxjs';
import {NgClass} from '@angular/common';
import {SortOrder} from '../../types/SortTypes';

interface PaginatedResponse {
  items: any[];
  currentPage: number;
  totalPages: number;
}

@Component({
  selector: 'app-data-table',
  imports: [
    NgClass
  ],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})
export class DataTable implements OnInit, OnChanges {
  // @Input() data: any[] = [];
  @Input() columns: { field: string, header: string }[] = [];
  @Input() searchColumns: string[] = [];
  @Input() filters: any[] = [];
  @Input() fetchData!: (page: number, itemsPerPage: number, filter:any, search: string, sortBy: string, orderBy: SortOrder) => Observable<PaginatedResponse>;
  
  data: any[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 15;
  totalPages: number = 1;
  loading: boolean = false;
  availablePageSizes = [15, 25, 50, 100];

  tableColumns = signal(this.columns);
  tableData = signal(this.data);
  tableSearchColumns = signal(this.searchColumns);
  tableDataLoading = signal(this.loading);
  tableFilters = signal(this.filters);
  error = signal(<string | null>null);

  currentSortBy = signal<string>("");
  currentSortOrder = signal<SortOrder>(SortOrder.ASC);

  private currentSearchQuery: string = '';

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.fetchData(this.currentPage, this.itemsPerPage, this.tableFilters(), this.currentSearchQuery, this.currentSortBy(),this.currentSortOrder()).subscribe({
      next: (response: any) => {
        this.data = response.items;
        this.totalPages = response.totalPages;
        this.tableData.set(response.items);
        console.log("Should load data: ", response.items);
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.error.set("Error: " + error.message);
      }
    })
  }

  // Lifecycle hook to detect changes in input properties
  ngOnChanges(changes: SimpleChanges) {
    if (changes['columns']) {
      this.tableColumns.set(this.columns);
    }
    if (changes['searchColumns']) {
      this.tableSearchColumns.set(this.searchColumns);
    }
    if (changes['filters']) {
      this.tableFilters.set(this.filters);
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadData();
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadData();
    }
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadData();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;

    if (this.totalPages <= maxVisible) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (this.currentPage <= 3) {
        for (let i = 1; i <= 4; i++) pages.push(i);
        pages.push(-1);
        pages.push(this.totalPages);
      } else if (this.currentPage >= this.totalPages - 2) {
        pages.push(1);
        pages.push(-1);
        for (let i = this.totalPages - 3; i <= this.totalPages; i++) pages.push(i);
      } else {
        pages.push(1);
        pages.push(-1);
        pages.push(this.currentPage - 1);
        pages.push(this.currentPage);
        pages.push(this.currentPage + 1);
        pages.push(-1);
        pages.push(this.totalPages);
      }
    }
    return pages;
  }


  onPageSizeChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.itemsPerPage = Number(target.value);
    this.currentPage = 1; // Go back to first page
    this.loadData();
  }

  getColumnValue(item: any, field: string): any {
    return item[field];
  }

  // Handle Sort logic triggered by clicking on column headers
  handleSort(field: string) {
    let newOrder: SortOrder = SortOrder.ASC;

    // If clicking the same column, toggle the order
    if (this.currentSortBy() === field) {
      newOrder = this.currentSortOrder() === SortOrder.ASC ? SortOrder.DESC : SortOrder.ASC;
    }

    this.currentSortBy.set(field);
    this.currentSortOrder.set(newOrder);

    this.loadData();
  }

  getSortIcon(field: string): string {
    if (this.currentSortBy() !== field) {
      return ''; // No icon for unsorted columns
    }
    return this.currentSortOrder() == SortOrder.ASC ? '↑' : '↓';
  }

  // Handle Search triggered by input event
  handleSearch(event: Event) {
    const target = event.target as HTMLInputElement;
    if (target && this.data) {
      this.currentSearchQuery = target.value.toLowerCase();
      this.loadData();
    }
  }

  // Handle Filter logic triggered by clicking on filter buttons
  toggleFilter(filter: any) {
    filter.active = !filter.active;
    this.tableFilters.set([...this.tableFilters()]);
    this.loadData();
  }
}
