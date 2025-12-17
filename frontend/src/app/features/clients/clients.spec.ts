import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Clients } from './clients';

describe('Clients', () => {
  let component: Clients;
  let fixture: ComponentFixture<Clients>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Clients],
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
