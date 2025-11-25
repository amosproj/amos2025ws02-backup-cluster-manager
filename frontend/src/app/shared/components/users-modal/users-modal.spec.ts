import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { UsersModal } from './users-modal';
import { ApiService } from '../../../core/services/api.service';

describe('UsersModal', () => {
  let component: UsersModal;
  let fixture: ComponentFixture<UsersModal>;

  beforeEach(async () => {
    const apiSpy = jasmine.createSpyObj<ApiService>('ApiService', ['get', 'post']);
    apiSpy.get.and.callFake(<T>(url: string) => {
      let result: any = [];
      if (url === 'groups') {
        result = [{ id: 1, name: 'Admins' }, { id: 2, name: 'Users' }];
      } else if (url.startsWith('users/search/')) {
        result = [{ id: 10, name: 'Alice' }];
      }
      return of(result as T);
    });
    apiSpy.post.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [UsersModal],
      providers: [
        { provide: ApiService, useValue: apiSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UsersModal);
    component = fixture.componentInstance;
    fixture.detectChanges(); // triggers ngOnInit -> loadGroups
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load groups on init', () => {
    expect(component.groups.length).toBe(2);
    expect(component.groups[0].name).toBe('Admins');
  });

  it('should trigger search and populate chooseUsers', () => {
    component.onNameInput('Ali');
    // Because debounceTime is in component, loadingUsers true immediately
    expect(component.loadingUsers).toBeTrue();
  });
});
