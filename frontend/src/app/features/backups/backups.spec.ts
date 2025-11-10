import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { Backups } from './backups';

describe('Backups', () => {
  let component: Backups;
  let fixture: ComponentFixture<Backups>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Backups],
      providers: [provideHttpClient()]
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
