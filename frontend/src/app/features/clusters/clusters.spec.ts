import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Clusters } from './clusters';

describe('Clusters', () => {
  let component: Clusters;
  let fixture: ComponentFixture<Clusters>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Clusters]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Clusters);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
