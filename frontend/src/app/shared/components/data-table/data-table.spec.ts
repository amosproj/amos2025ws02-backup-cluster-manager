import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DataTable } from './data-table';
import { SimpleChange } from '@angular/core';

describe('DataTable', () => {
  let component: DataTable;
  let fixture: ComponentFixture<DataTable>;

  const mockData = [
    { id: 1, name: 'Node 1', status: 'Active' },
    { id: 2, name: 'Node 2', status: 'Inactive' },
    { id: 3, name: 'Test Node', status: 'Active' }
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
      const filters = [{ label: 'Active', active: false }];

      component.data = mockData;
      component.columns = columns;
      component.filters = filters;
      component.loading = true;

      component.ngOnChanges({
        data: new SimpleChange(null, mockData, true),
        columns: new SimpleChange(null, columns, true),
        filters: new SimpleChange(null, filters, true),
        loading: new SimpleChange(null, true, true)
      });

      expect(component.tableData()).toEqual(mockData);
      expect(component.tableColumns()).toEqual(columns);
      expect(component.tableFilters()).toEqual(filters);
      expect(component.tableDataLoading()).toBe(true);
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
    it('should emit search query to parent', () => {
      spyOn(component.searchChange, 'emit');
      
      const event = { target: { value: 'Node 1' } } as any;
      component.handleSearch(event);
      
      expect(component.searchChange.emit).toHaveBeenCalledWith('Node 1');
    });

    it('should emit empty string when search is cleared', () => {
      spyOn(component.searchChange, 'emit');
      
      const event = { target: { value: '   ' } } as any;
      component.handleSearch(event);
      
      expect(component.searchChange.emit).toHaveBeenCalledWith('');
    });

    it('should trim whitespace from search query', () => {
      spyOn(component.searchChange, 'emit');
      
      const event = { target: { value: '  Node 1  ' } } as any;
      component.handleSearch(event);
      
      expect(component.searchChange.emit).toHaveBeenCalledWith('Node 1');
    });
  });

  describe('toggleFilter', () => {
    it('should toggle filter active state', () => {
      component.filters = [{ label: 'Active', active: false }];
      component.ngOnChanges({
        filters: new SimpleChange(null, component.filters, true)
      });

      const filter = component.tableFilters()[0];
      expect(filter.active).toBe(false);

      component.toggleFilter(filter);
      expect(filter.active).toBe(true);
    });

    it('should emit updated filters to parent', () => {
      spyOn(component.filtersChange, 'emit');
      
      component.filters = [{ label: 'Active', active: false }];
      component.ngOnChanges({
        filters: new SimpleChange(null, component.filters, true)
      });

      const filter = component.tableFilters()[0];
      component.toggleFilter(filter);

      expect(component.filtersChange.emit).toHaveBeenCalled();
      const emittedFilters = (component.filtersChange.emit as jasmine.Spy).calls.mostRecent().args[0];
      expect(emittedFilters[0].active).toBe(true);
    });

    it('should update signal with new array reference', () => {
      component.filters = [{ label: 'Active', active: false }];
      component.ngOnChanges({
        filters: new SimpleChange(null, component.filters, true)
      });

      const initialRef = component.tableFilters();
      const filter = component.tableFilters()[0];
      
      component.toggleFilter(filter);
      
      const newRef = component.tableFilters();
      expect(newRef).not.toBe(initialRef); // New reference created
      expect(newRef[0].active).toBe(true);
    });
  });

  describe('data binding', () => {
    it('should display data passed from parent', () => {
      component.data = mockData;
      component.ngOnChanges({
        data: new SimpleChange(null, mockData, true)
      });

      expect(component.tableData()).toEqual(mockData);
      expect(component.tableData().length).toBe(3);
    });

    it('should update when parent provides filtered data', () => {
      const filteredData = [{ id: 1, name: 'Node 1', status: 'Active' }];
      
      component.data = filteredData;
      component.ngOnChanges({
        data: new SimpleChange(mockData, filteredData, false)
      });

      expect(component.tableData()).toEqual(filteredData);
      expect(component.tableData().length).toBe(1);
    });
  });
});