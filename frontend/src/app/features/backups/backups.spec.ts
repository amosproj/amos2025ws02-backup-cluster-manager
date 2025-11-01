import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Backups } from './backups';

describe('Backups', () => {
  let component: Backups;
  let fixture: ComponentFixture<Backups>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Backups]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Backups);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
