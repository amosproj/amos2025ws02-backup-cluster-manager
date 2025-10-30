import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => null } }, // Mock snapshot if used
            queryParams: of({}), // Mock queryParams observable
          },
        },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render header with Cluster Manager text', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const headerText = compiled.querySelector('#header-text');

    expect(headerText?.textContent).toContain('Cluster Manager');
  });

  it('should provide navigation items with correct structure', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const navItems = compiled.querySelectorAll('.sidebar-navigation-item');

    expect(navItems.length).toBeGreaterThan(0);
    navItems.forEach(link => {
      const icon = link.querySelector('img');
      const label = link.querySelector('span');
      expect(icon).toBeTruthy();
      expect(label).toBeTruthy();
      expect(label?.textContent?.trim()).toBeTruthy();
    });
  });
});
