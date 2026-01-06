import {ComponentFixture, TestBed} from '@angular/core/testing';

import {Clients} from './clients';
import {ApiService} from '../../core/services/api.service';
import {AuthService} from '../../core/services/auth.service';
import {of} from 'rxjs';
import UserPermissionsEnum from '../../shared/types/Permissions';

describe('Clients', () => {
  let component: Clients;
  let fixture: ComponentFixture<Clients>;

  const apiServiceMock = {
    loading$: of(false),
    post: jasmine.createSpy('post').and.returnValue(of({})),
    get: jasmine.createSpy('get').and.returnValue(of({}))
  };

  const authServiceMock = {
    hasPermission: (permission: UserPermissionsEnum) => true
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Clients],
      providers: [
        {provide: ApiService, useValue: apiServiceMock},
        {provide: AuthService, useValue: authServiceMock}
      ],
    })
      .compileComponents();

    fixture = TestBed.createComponent(Clients);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
