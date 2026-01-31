import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { Permissions } from './permissions';

describe('Permissions', () => {
  let component: Permissions;
  let fixture: ComponentFixture<Permissions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Permissions],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Permissions);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
