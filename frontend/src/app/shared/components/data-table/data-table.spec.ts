import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DataTable } from './data-table';
import { SimpleChange } from '@angular/core';

describe('DataTable', () => {
  let component: DataTable;
  let fixture: ComponentFixture<DataTable>;

  const mockData = [
    { id: 1, name: 'Node 1', status: 'running' },
    { id: 2, name: 'Node 2', status: 'stopped' },
    { id: 3, name: 'Test Node', status: 'running' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTable]
    }).compileComponents();

    fixture = TestBed.createComponent(DataTable);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should update signals when inputs change', () => {
      const columns = [{ field: 'id', header: 'ID' }];
      const filters = [{ label: 'Test', filterFn: () => true, active: false }];

      // Set inputs first
      component.data = mockData;
      component.columns = columns;
      component.filters = filters;
      component.loading = true;
      component.searchColumns = ['name'];

      // Then trigger ngOnChanges
      component.ngOnChanges({
        data: new SimpleChange(null, mockData, true),
        columns: new SimpleChange(null, columns, true),
        filters: new SimpleChange(null, filters, true),
        loading: new SimpleChange(null, true, true),
        searchColumns: new SimpleChange(null, ['name'], true)
      });

      expect(component.tableData()).toEqual(mockData);
      expect(component.tableColumns()).toEqual(columns);
      expect(component.tableFilters()).toEqual(filters);
      expect(component.tableDataLoading()).toBe(true);
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

  describe('handleSearch', () => {
    beforeEach(() => {
      component.data = mockData;
      component.searchColumns = ['name', 'status'];
      component.ngOnChanges({
        data: new SimpleChange(null, mockData, true),
        searchColumns: new SimpleChange(null, ['name', 'status'], true)
      });
    });

    it('should filter data by search query (case insensitive)', () => {
      component.handleSearch({ target: { value: 'node 1' } } as any);
      expect(component.tableData().length).toBe(1);
      expect(component.tableData()[0].name).toBe('Node 1');
    });

    it('should show all data when search is empty', () => {
      component.handleSearch({ target: { value: '' } } as any);
      expect(component.tableData().length).toBe(mockData.length);
    });

    it('should search across multiple columns', () => {
      component.handleSearch({ target: { value: 'running' } } as any);
      expect(component.tableData().length).toBe(2);
    });
  });

  describe('toggleFilter', () => {
    beforeEach(() => {
      component.data = mockData;
      component.filters = [
        { label: 'Running', filterFn: (item: any) => item.status === 'running', active: false }
      ];
      component.ngOnChanges({
        data: new SimpleChange(null, mockData, true),
        filters: new SimpleChange(null, component.filters, true)
      });
    });

    it('should toggle filter state and apply filtering', () => {
      const filter = component.tableFilters()[0];

      component.toggleFilter(filter);
      expect(filter.active).toBe(true);
      expect(component.tableData().length).toBe(2);
      expect(component.tableData().every(item => item.status === 'running')).toBe(true);

      component.toggleFilter(filter);
      expect(filter.active).toBe(false);
      expect(component.tableData().length).toBe(mockData.length);
    });
  });

  describe('combined search and filters', () => {
    beforeEach(() => {
      component.data = mockData;
      component.searchColumns = ['name'];
      component.filters = [
        { label: 'Running', filterFn: (item: any) => item.status === 'running', active: false }
      ];
      component.ngOnChanges({
        data: new SimpleChange(null, mockData, true),
        searchColumns: new SimpleChange(null, ['name'], true),
        filters: new SimpleChange(null, component.filters, true)
      });
    });

    it('should combine search and active filters', () => {
      component.handleSearch({ target: { value: 'Node' } } as any);
      expect(component.tableData().length).toBe(3); // Node 1, Node 2, and Test Node

      component.toggleFilter(component.tableFilters()[0]);
      expect(component.tableData().length).toBe(2); // Node 1 and Test Node (both running)
      expect(component.tableData()[0].name).toBe('Node 1');
    });
  });
});

