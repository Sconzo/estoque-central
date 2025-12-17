import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MetricCardComponent } from './metric-card.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('MetricCardComponent', () => {
  let component: MetricCardComponent;
  let fixture: ComponentFixture<MetricCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MetricCardComponent, NoopAnimationsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(MetricCardComponent);
    component = fixture.componentInstance;
    component.title = 'Test Metric';
    component.value = '100';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render title correctly', () => {
    const titleElement = fixture.nativeElement.querySelector('mat-card-title');
    expect(titleElement.textContent).toContain('Test Metric');
  });

  it('should render value correctly', () => {
    const valueElement = fixture.nativeElement.querySelector('.metric-value');
    expect(valueElement.textContent.trim()).toBe('100');
  });

  it('should render positive changePercent with up arrow', () => {
    component.changePercent = 12.5;
    fixture.detectChanges();

    const changeElement = fixture.nativeElement.querySelector('.metric-change');
    expect(changeElement.textContent).toContain('↑');
    expect(changeElement.textContent).toContain('+12.5%');
    expect(changeElement.classList.contains('positive')).toBe(true);
  });

  it('should render negative changePercent with down arrow', () => {
    component.changePercent = -8.3;
    fixture.detectChanges();

    const changeElement = fixture.nativeElement.querySelector('.metric-change');
    expect(changeElement.textContent).toContain('↓');
    expect(changeElement.textContent).toContain('-8.3%');
    expect(changeElement.classList.contains('negative')).toBe(true);
  });

  it('should not render changePercent section when undefined', () => {
    component.changePercent = undefined;
    fixture.detectChanges();

    const changeElement = fixture.nativeElement.querySelector('.metric-change');
    expect(changeElement).toBeNull();
  });

  it('should have correct aria-label on card', () => {
    const card = fixture.nativeElement.querySelector('mat-card');
    expect(card.getAttribute('aria-label')).toBe('Test Metric metric');
  });

  it('should have aria-live="polite" on value element', () => {
    const valueElement = fixture.nativeElement.querySelector('.metric-value');
    expect(valueElement.getAttribute('aria-live')).toBe('polite');
  });

  it('should apply correct border color', () => {
    component.color = '#FF0000';
    fixture.detectChanges();

    const card = fixture.nativeElement.querySelector('mat-card');
    expect(card.style.borderLeft).toBe('4px solid rgb(255, 0, 0)');
  });

  it('should render icon with correct color', () => {
    component.icon = 'trending_up';
    component.color = '#6A1B9A';
    fixture.detectChanges();

    const icon = fixture.nativeElement.querySelector('mat-icon');
    expect(icon.textContent.trim()).toBe('trending_up');
    expect(icon.style.color).toBe('rgb(106, 27, 154)');
  });

  it('should have correct ARIA label on changePercent', () => {
    component.changePercent = 15.5;
    fixture.detectChanges();

    const changeSpan = fixture.nativeElement.querySelector('.metric-change span');
    expect(changeSpan.getAttribute('aria-label')).toBe('Mudança do período anterior: 15.5%');
  });
});
