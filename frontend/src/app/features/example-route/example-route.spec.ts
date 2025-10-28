import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExampleRoute } from './example-route';

describe('ExampleRoute', () => {
  let component: ExampleRoute;
  let fixture: ComponentFixture<ExampleRoute>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExampleRoute]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExampleRoute);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
