import { TestBed, ComponentFixture } from '@angular/core/testing';
import { of } from 'rxjs';
import { Backups } from './backups';
import { SortOrder } from '../../shared/types/SortTypes';
import {BackupsService} from './backups.service';
import {ApiService} from '../../core/services/api.service';

describe('Backups', () => {
  let fixture: ComponentFixture<Backups>;
  let component: Backups;

  const backupsServiceMock = {
    getBackups: (page = 1, itemsPerPage = 15, filters = '', search = '', sortBy = '', order: SortOrder = SortOrder.ASC) =>
      of({ items: [], currentPage: page, totalPages: 1 }),
    createBackup: jasmine.createSpy('createBackup').and.returnValue(of({})),
    deleteBackup: jasmine.createSpy('deleteBackup').and.returnValue(of({})),
  };

  const apiServiceMock = {
    loading$: of(false),
    post: jasmine.createSpy('post').and.returnValue(of({})),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Backups],
      providers: [
        { provide: BackupsService, useValue: backupsServiceMock },
        { provide: ApiService, useValue: apiServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Backups);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
});
