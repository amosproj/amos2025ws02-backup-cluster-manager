import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DataTable } from './data-table';
import { SimpleChange } from '@angular/core';
import { of } from 'rxjs';
import { SortOrder } from '../../types/SortTypes';

interface FetchCallArgs {
  page: number;
  itemsPerPage: number;
  filter: string;
  search: string;
  sortBy: string;
  orderBy: SortOrder;
}

describe('DataTable', () => {
  let component: DataTable;
  let fixture: ComponentFixture<DataTable>;
  let fetchDataSpy: jasmine.Spy;
  let lastFetchArgs: FetchCallArgs;

  const mockData = [
    { id: 1, name: 'Node 1', status: 'running' },
    { id: 2, name: 'Node 2', status: 'stopped' },
    { id: 3, name: 'Test Node', status: 'running' }
  ];

  const expectLastFetchArgs = (expected: Partial<FetchCallArgs>) => {
    expect(lastFetchArgs).toEqual(jasmine.objectContaining(expected));
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTable]
    }).compileComponents();

    fixture = TestBed.createComponent(DataTable);
    component = fixture.componentInstance;

    fetchDataSpy = jasmine.createSpy('fetchData').and.callFake((page: number, itemsPerPage: number, filter: string, search: string, sortBy: string, orderBy: SortOrder) => {
      lastFetchArgs = { page, itemsPerPage, filter, search, sortBy, orderBy };
      return of({
        items: mockData,
        currentPage: page,
        totalPages: 3
      });
    });

    component.fetchData = fetchDataSpy;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should update signals when inputs change', () => {
      const columns = [{ field: 'id', header: 'ID' }];
      const filters = [{ label: 'Test', filterFn: () => true, active: false }];

      component.columns = columns;
      component.filters = filters;
      component.searchColumns = ['name'];

      component.ngOnChanges({
        columns: new SimpleChange(null, columns, true),
        filters: new SimpleChange(null, filters, true),
        searchColumns: new SimpleChange(null, ['name'], true)
      });

      expect(component.tableColumns()).toEqual(columns);
      expect(component.tableFilters()).toEqual(filters);
      expect(component.tableSearchColumns()).toEqual(['name']);
    });
  });

  describe('getColumnValue', () => {
    it('should return field value from item', () => {
      const item = { id: 1, name: 'Test' };
      expect(component.getColumnValue(item, 'name')).toBe('Test');
      expect(component.getColumnValue(item, 'missing')).toBeUndefined();
    });
  });

  describe('server interactions', () => {
    it('should load data on init', () => {
      component.ngOnInit();
      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({
        page: 1,
        itemsPerPage: 15,
        filter: '',
        search: '',
        sortBy: '',
        orderBy: SortOrder.ASC
      });
      expect(component.tableData()).toEqual(mockData);
      expect(component.totalPages).toBe(3);
    });

    it('should pass lowercase search query to fetchData', () => {
      component.ngOnInit();
      fetchDataSpy.calls.reset();

      component.handleSearch({ target: { value: 'Node 1' } } as any);

      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({ search: 'node 1' });
    });

    it('should clear search query when input is empty', () => {
      component.ngOnInit();
      component.handleSearch({ target: { value: 'Node' } } as any);
      fetchDataSpy.calls.reset();

      component.handleSearch({ target: { value: '' } } as any);

      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({ search: '' });
    });

    it('should toggle filters and forward active labels', () => {
      component.filters = [
        { label: 'Running', filterFn: () => true, active: false },
        { label: 'Stopped', filterFn: () => true, active: false }
      ];
      component.ngOnChanges({
        filters: new SimpleChange(null, component.filters, true)
      });

      component.ngOnInit();
      fetchDataSpy.calls.reset();

      const filters = component.tableFilters();
      component.toggleFilter(filters[0]);
      expect(filters[0].active).toBeTrue();
      expectLastFetchArgs({ filter: 'Running' });

      component.toggleFilter(filters[1]);
      expect(filters[1].active).toBeTrue();
      expectLastFetchArgs({ filter: 'Running,Stopped' });

      component.toggleFilter(filters[0]);
      expectLastFetchArgs({ filter: 'Stopped' });
    });

    it('should fetch next page when available', () => {
      component.ngOnInit();
      component.totalPages = 3;
      component.currentPage = 1;
      fetchDataSpy.calls.reset();

      component.nextPage();

      expect(component.currentPage).toBe(2);
      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({ page: 2 });
    });

    it('should fetch previous page when available', () => {
      component.ngOnInit();
      component.totalPages = 3;
      component.currentPage = 2;
      fetchDataSpy.calls.reset();

      component.previousPage();

      expect(component.currentPage).toBe(1);
      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({ page: 1 });
    });

    it('should only go to valid pages', () => {
      component.ngOnInit();
      component.totalPages = 3;
      fetchDataSpy.calls.reset();

      component.goToPage(3);
      expect(component.currentPage).toBe(3);
      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({ page: 3 });

      fetchDataSpy.calls.reset();
      component.goToPage(10);
      expect(fetchDataSpy).not.toHaveBeenCalled();
      expect(component.currentPage).toBe(3);
    });

    it('should reset to first page when page size changes', () => {
      component.ngOnInit();
      component.currentPage = 2;
      fetchDataSpy.calls.reset();

      component.onPageSizeChange({ target: { value: '25' } } as any);

      expect(component.itemsPerPage).toBe(25);
      expect(component.currentPage).toBe(1);
      expect(fetchDataSpy).toHaveBeenCalledTimes(1);
      expectLastFetchArgs({ page: 1, itemsPerPage: 25 });
    });

    it('should toggle sorting and forward sort params', () => {
      component.ngOnInit();
      fetchDataSpy.calls.reset();

      component.handleSort('name');
      expect(component.currentSortBy()).toBe('name');
      expect(component.currentSortOrder()).toBe(SortOrder.ASC);
      expectLastFetchArgs({ sortBy: 'name', orderBy: SortOrder.ASC });

      fetchDataSpy.calls.reset();
      component.handleSort('name');
      expect(component.currentSortOrder()).toBe(SortOrder.DESC);
      expectLastFetchArgs({ sortBy: 'name', orderBy: SortOrder.DESC });

      fetchDataSpy.calls.reset();
      component.handleSort('status');
      expect(component.currentSortBy()).toBe('status');
      expect(component.currentSortOrder()).toBe(SortOrder.ASC);
      expectLastFetchArgs({ sortBy: 'status', orderBy: SortOrder.ASC });
    });
  });
});
