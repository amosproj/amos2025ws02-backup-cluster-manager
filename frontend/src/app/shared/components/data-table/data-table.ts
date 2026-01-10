import { Component, EventEmitter, Input, OnChanges, OnInit, Output, signal, SimpleChanges, TemplateRef, AfterViewInit } from '@angular/core';
import { initFlowbite } from 'flowbite';
import { Observable } from 'rxjs';
import { NgClass, NgTemplateOutlet, CommonModule } from '@angular/common';
import { SortOrder } from '../../types/SortTypes';
import { FormsModule } from '@angular/forms';

interface PaginatedResponse {
  items: any[];

  currentPage: number;
  totalPages: number;
}

@Component({
  selector: 'app-data-table',
  imports: [
    NgClass,
    FormsModule,
    NgTemplateOutlet,
    CommonModule
  ],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})
export class DataTable implements OnInit, OnChanges, AfterViewInit {
  // @Input() data: any[] = [];
  @Input() columns: { field: string, header: string }[] = [];
  @Input() searchColumns: string[] = [];
  @Input() filters: any[] = [];
  @Input() isNodeButtonEnabled = true;
  @Input() fetchData!: (page: number, itemsPerPage: number, filter: string, search: string, sortBy: string, orderBy: SortOrder) => Observable<PaginatedResponse>;
  @Input() loading: boolean | null = false;
  @Output() selectionChange = new EventEmitter<any[]>();

  data: any[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 15;
  totalPages: number = 1;
  availablePageSizes = [1, 2, 3, 15, 25, 50, 100];
  filterParam = "";
  selectedIds = new Set<string>();

  @Input() addButtonText = 'Add';
  @Input() editButtonText = 'Edit';
  @Input() deleteButtonText = 'Delete';

  @Input() showAddButton = true;
  @Input() showDeleteButton = false;
  @Input() showEditButton = false;
  @Input() showActionsColumn = false;
  @Input() rowActions: TemplateRef<any> | null = null;
  @Input() addButtonTemplate: TemplateRef<unknown> | null = null;
  @Input() editButtonTemplate: TemplateRef<unknown> | null = null;

  @Output() add = new EventEmitter<void>();
  @Output() edit = new EventEmitter<void>();

  currentAddButtonText = this.addButtonText;
  currentEditButtonText = this.editButtonText;
  currentDeleteButtonText = this.deleteButtonText;

  @Output() deleteSelection = new EventEmitter<any[]>();
  @Input() showToggleButton = false;
  @Output() toggleChanged = new EventEmitter<any>();


  toggleManaged(item: any) {
    item.isManaged = !item?.isManaged;
    this.toggleChanged.emit(item);
  }


  tableColumns = signal(this.columns);
  tableData = signal(this.data);
  tableSearchColumns = signal(this.searchColumns);
  tableDataLoading = signal(this.loading);
  tableFilters = signal(this.filters);
  error = signal(<string | null>null);

  currentSortBy = signal<string>("");
  currentSortOrder = signal<SortOrder>(SortOrder.ASC);

  uniqueId = 'table-' + Math.random().toString(36).substr(2, 9);

  private currentSearchQuery: string = '';

  ngOnInit() {
    this.loadData();
  }

  ngAfterViewInit() {
    initFlowbite();
  }

  isRowSelected(row: any): boolean {
    return this.selectedIds.has(row.id);
  }

  allRowsSelected(): boolean {
    const data = this.tableData();
    return data.length > 0 && data.every(row => this.selectedIds.has(row.id));
  }

  toggleSelectAll(event: Event): void {
    const input = event.target as HTMLInputElement;
    const checked = input?.checked ?? false;

    if (checked) {
      this.tableData().forEach(row => this.selectedIds.add(row.id));
    } else {
      this.selectedIds.clear();
    }
  }

  hasSelection(): boolean {
    return this.selectedIds && this.selectedIds.size > 0;
  }

  hasSingleSelection(): boolean {
    return this.selectedIds && this.selectedIds.size === 1;
  }

  toggleRowSelection(row: any, event: Event): void {
    const input = event.target as HTMLInputElement;
    const checked = input?.checked ?? false;

    if (checked) {
      this.selectedIds.add(row.id);
    } else {
      this.selectedIds.delete(row.id);
    }
  }


  private emitSelectionChange(): void {
    const data = this.tableData();
    const selectedRows = data.filter(row => this.selectedIds.has(row.id));
    this.selectionChange.emit(selectedRows);
  }


  loadData() {
    this.loading = true;
    this.fetchData(this.currentPage, this.itemsPerPage, this.filterParam, this.currentSearchQuery, this.currentSortBy(), this.currentSortOrder()).subscribe({
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
  onDeleteClick(): void {
    const selectedRows = this.tableData().filter(row => this.selectedIds.has(row.id));
    this.deleteSelection.emit(selectedRows);
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
    if (changes['addButtonText'] && !changes['addButtonText'].firstChange) {
      this.currentAddButtonText = this.addButtonText;
    }
    if (changes['editButtonText'] && !changes['editButtonText'].firstChange) {
      this.currentEditButtonText = this.editButtonText;
    }
    if (changes['deleteButtonText'] && !changes['deleteButtonText'].firstChange) {
      this.currentDeleteButtonText = this.deleteButtonText;
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
    // return item[field];
    return field.split('.').reduce((obj, key) => obj?.[key], item);
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
    this.filterParam = this.tableFilters().filter(f => f.active).map(f => f.label).join(",");
    this.loadData();
  }

  onAddClick() {
    this.add.emit();
  }
  onEditClick() {
    const selectedRows = this.tableData().find(row => this.selectedIds.has(row.id));
    this.edit.emit(selectedRows);
  }
}
