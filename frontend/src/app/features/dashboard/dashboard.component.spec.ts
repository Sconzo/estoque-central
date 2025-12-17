import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent, NoopAnimationsModule],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with loading true', () => {
    expect(component.loading()).toBe(true);
  });

  it('should load mocked data on init', async () => {
    component.ngOnInit();
    await new Promise(resolve => setTimeout(resolve, 1000)); // Wait for carregarDados
    fixture.detectChanges();

    expect(component.loading()).toBe(false);
    expect(component.stats().totalProdutos).toBe(156);
    expect(component.stats().vendasMes).toBe(234);
  });

  it('should render stats grid when not loading', () => {
    component.loading.set(false);
    fixture.detectChanges();

    const statsGrid = fixture.nativeElement.querySelector('.stats-grid');
    expect(statsGrid).toBeTruthy();
  });

  it('should have 4 metric cards in stats data', () => {
    const stats = component.stats();
    expect(stats.totalProdutos).toBeDefined();
    expect(stats.estoqueTotal).toBeDefined();
    expect(stats.vendasMes).toBeDefined();
    expect(stats.clientesAtivos).toBeDefined();
  });

  it('should render action buttons grid', () => {
    component.loading.set(false);
    fixture.detectChanges();

    const actionsGrid = fixture.nativeElement.querySelector('.actions-grid');
    expect(actionsGrid).toBeTruthy();

    const buttons = actionsGrid.querySelectorAll('button');
    expect(buttons.length).toBe(4);
  });

  it('should use Material icons (not emojis)', () => {
    component.loading.set(false);
    fixture.detectChanges();

    const matIcons = fixture.nativeElement.querySelectorAll('mat-icon');
    expect(matIcons.length).toBeGreaterThan(0);
  });

  it('should calculate alert class correctly', () => {
    expect(component.getAlertClass(2, 10)).toBe('alert-critical'); // 20% < 30%
    expect(component.getAlertClass(5, 10)).toBe('alert-warning');  // 50% < 60%
    expect(component.getAlertClass(8, 10)).toBe('alert-ok');       // 80% >= 60%
  });

  it('should render products with low stock', async () => {
    component.loading.set(false);
    component.produtosEstoqueBaixo.set([
      { id: 1, nome: 'Produto A', estoque: 3, minimo: 10, codigo: 'PROD-001' }
    ]);
    fixture.detectChanges();

    const table = fixture.nativeElement.querySelector('.data-table');
    expect(table).toBeTruthy();

    const rows = table.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
  });

  it('should display header icons for sections', () => {
    component.loading.set(false);
    fixture.detectChanges();

    const headerIcons = fixture.nativeElement.querySelectorAll('.header-icon');
    expect(headerIcons.length).toBeGreaterThan(0);
  });
});
