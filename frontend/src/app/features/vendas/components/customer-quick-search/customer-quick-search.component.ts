import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { CustomerQuick } from '../../models/customer.model';
import { debounceTime, Subject } from 'rxjs';

/**
 * CustomerQuickSearchComponent - Autocomplete search component
 * Story 4.1: Customer Management - AC8
 */
@Component({
  selector: 'app-customer-quick-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="quick-search">
      <input
        type="text"
        [(ngModel)]="searchQuery"
        (ngModelChange)="onSearchChange($event)"
        placeholder="Buscar cliente..."
        class="form-control"
      />
      <div *ngIf="results.length > 0" class="results">
        <div
          *ngFor="let customer of results"
          (click)="selectCustomer(customer)"
          class="result-item"
        >
          <strong>{{ customer.displayName }}</strong>
          <span>{{ customer.cpf || customer.cnpj }}</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .quick-search {
      position: relative;
    }
    .results {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 1px solid #ddd;
      border-radius: 4px;
      max-height: 300px;
      overflow-y: auto;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      z-index: 1000;
    }
    .result-item {
      padding: 0.75rem;
      cursor: pointer;
      border-bottom: 1px solid #f0f0f0;
    }
    .result-item:hover {
      background: #f8f9fa;
    }
  `]
})
export class CustomerQuickSearchComponent {
  @Output() customerSelected = new EventEmitter<CustomerQuick>();

  searchQuery = '';
  results: CustomerQuick[] = [];
  private searchSubject = new Subject<string>();

  constructor(private customerService: CustomerService) {
    this.searchSubject.pipe(debounceTime(300)).subscribe(query => {
      if (query.length >= 2) {
        this.search(query);
      } else {
        this.results = [];
      }
    });
  }

  onSearchChange(query: string): void {
    this.searchSubject.next(query);
  }

  search(query: string): void {
    this.customerService.quickSearch(query).subscribe({
      next: (results) => {
        this.results = results;
      },
      error: (err) => console.error('Search error:', err)
    });
  }

  selectCustomer(customer: CustomerQuick): void {
    this.customerSelected.emit(customer);
    this.results = [];
    this.searchQuery = customer.displayName;
  }
}
