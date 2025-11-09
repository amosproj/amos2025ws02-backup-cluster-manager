import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { Nodes } from './nodes';

describe('Nodes', () => {
  let component: Nodes;
  let fixture: ComponentFixture<Nodes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Nodes],
      providers: [provideHttpClient()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Nodes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
