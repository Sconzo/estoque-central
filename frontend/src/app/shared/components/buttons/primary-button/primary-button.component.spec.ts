import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PrimaryButtonComponent } from './primary-button.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('PrimaryButtonComponent', () => {
  let component: PrimaryButtonComponent;
  let fixture: ComponentFixture<PrimaryButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrimaryButtonComponent, NoopAnimationsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(PrimaryButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit onClick event when button is clicked', () => {
    spyOn(component.onClick, 'emit');

    const button = fixture.nativeElement.querySelector('button');
    button.click();

    expect(component.onClick.emit).toHaveBeenCalled();
  });

  it('should be disabled when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.disabled).toBe(true);
  });

  it('should be disabled when disabled is true', () => {
    component.disabled = true;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.disabled).toBe(true);
  });

  it('should display loadingText when loading is true', () => {
    component.label = 'Save';
    component.loadingText = 'Saving...';
    component.loading = true;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.textContent).toContain('Saving...');
  });

  it('should display label when loading is false', () => {
    component.label = 'Save';
    component.loading = false;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.textContent).toContain('Save');
  });

  it('should have correct aria-label attribute', () => {
    component.ariaLabel = 'Test button';
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.getAttribute('aria-label')).toBe('Test button');
  });

  it('should have aria-busy="true" when loading', () => {
    component.loading = true;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.getAttribute('aria-busy')).toBe('true');
  });

  it('should have aria-busy="false" when not loading', () => {
    component.loading = false;
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button');
    expect(button.getAttribute('aria-busy')).toBe('false');
  });
});
