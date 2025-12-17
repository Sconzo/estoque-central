import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VariantService } from '../../services/variant.service';
import { VariantAttribute, ProductVariant, VariantMatrixRow } from '../../models/variant.model';
import { FeedbackService } from '../../../../shared/services/feedback.service';

/**
 * VariantMatrixComponent - Manage product variants matrix
 *
 * Features:
 * - Define attributes (max 3) with multiple values
 * - Generate variant matrix (cartesian product)
 * - Edit SKU, price, cost inline
 * - Delete individual variants
 * - Validate max 100 variants
 */
@Component({
  selector: 'app-variant-matrix',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './variant-matrix.component.html',
  styleUrls: ['./variant-matrix.component.scss']
})
export class VariantMatrixComponent implements OnInit {
  @Input() parentProductId: string | null = null;
  @Input() baseSku: string = '';
  @Input() basePrice: number = 0;
  @Input() baseCost: number = 0;
  @Output() variantsGenerated = new EventEmitter<ProductVariant[]>();

  // Attributes definition
  attributes: VariantAttribute[] = [];
  newAttributeName = '';
  newAttributeValue = '';
  selectedAttributeIndex: number | null = null;

  // Generated variants
  generatedVariants: VariantMatrixRow[] = [];
  loading = false;
  error: string | null = null;

  // Calculated values
  estimatedVariants = 0;

  constructor(
    private variantService: VariantService,
    private feedback: FeedbackService
  ) {}

  ngOnInit(): void {
    // Initialize with one empty attribute
    if (this.attributes.length === 0) {
      this.addAttribute();
    }
  }

  /**
   * Adds new attribute (max 3)
   */
  addAttribute(): void {
    if (this.attributes.length >= 3) {
      this.feedback.showWarning('Máximo de 3 atributos permitidos');
      return;
    }

    this.attributes.push({
      name: '',
      values: []
    });
  }

  /**
   * Removes attribute
   */
  removeAttribute(index: number): void {
    this.attributes.splice(index, 1);
    this.calculateEstimatedVariants();
  }

  /**
   * Adds value to attribute
   */
  addValueToAttribute(index: number): void {
    const attribute = this.attributes[index];

    if (!attribute.name || attribute.name.trim() === '') {
      this.feedback.showWarning('Por favor, defina o nome do atributo primeiro');
      return;
    }

    const value = prompt(`Adicionar valor para "${attribute.name}":`);

    if (value && value.trim() !== '') {
      if (attribute.values.includes(value.trim())) {
        this.feedback.showWarning('Este valor já existe');
        return;
      }

      attribute.values.push(value.trim());
      this.calculateEstimatedVariants();
    }
  }

  /**
   * Removes value from attribute
   */
  removeValue(attributeIndex: number, valueIndex: number): void {
    this.attributes[attributeIndex].values.splice(valueIndex, 1);
    this.calculateEstimatedVariants();
  }

  /**
   * Calculates estimated number of variants
   */
  calculateEstimatedVariants(): void {
    this.estimatedVariants = this.attributes
      .filter(attr => attr.name && attr.values.length > 0)
      .reduce((acc, attr) => acc * attr.values.length, 1);
  }

  /**
   * Validates attributes before generating
   */
  validateAttributes(): boolean {
    if (this.attributes.length === 0) {
      this.error = 'Adicione pelo menos um atributo';
      return false;
    }

    for (const attr of this.attributes) {
      if (!attr.name || attr.name.trim() === '') {
        this.error = 'Todos os atributos devem ter um nome';
        return false;
      }

      if (attr.values.length === 0) {
        this.error = `Atributo "${attr.name}" não tem valores`;
        return false;
      }
    }

    if (this.estimatedVariants > 100) {
      this.error = `Máximo de 100 variantes permitidas. Esta combinação geraria ${this.estimatedVariants} variantes.`;
      return false;
    }

    return true;
  }

  /**
   * Generates variant matrix
   */
  generateMatrix(): void {
    this.error = null;

    if (!this.validateAttributes()) {
      return;
    }

    if (!this.parentProductId) {
      this.error = 'ID do produto pai não informado';
      return;
    }

    this.loading = true;

    // Generate combinations locally for preview
    const combinations = this.generateCombinations();

    // Create matrix rows
    this.generatedVariants = combinations.map(combination => ({
      sku: this.generateSku(combination),
      combination,
      price: this.basePrice,
      cost: this.baseCost,
      editable: true
    }));

    this.loading = false;
  }

  /**
   * Saves variants to backend
   */
  saveVariants(): void {
    if (!this.parentProductId) {
      this.error = 'ID do produto pai não informado';
      return;
    }

    this.loading = true;
    this.error = null;

    // Call backend to generate and save variants
    this.variantService.generateVariants(this.parentProductId, this.attributes).subscribe({
      next: (variants) => {
        this.loading = false;
        this.variantsGenerated.emit(variants);
        this.feedback.showSuccess(`${variants.length} variantes criadas com sucesso!`);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Erro ao gerar variantes: ' + (err.error?.message || err.message || 'Erro desconhecido');
        console.error('Error generating variants:', err);
      }
    });
  }

  /**
   * Generates all combinations (cartesian product)
   */
  private generateCombinations(): Record<string, string>[] {
    if (this.attributes.length === 0) {
      return [];
    }

    let result: Record<string, string>[] = [{}];

    for (const attribute of this.attributes) {
      if (!attribute.name || attribute.values.length === 0) continue;

      const temp: Record<string, string>[] = [];

      for (const existing of result) {
        for (const value of attribute.values) {
          temp.push({
            ...existing,
            [attribute.name]: value
          });
        }
      }

      result = temp;
    }

    return result;
  }

  /**
   * Generates SKU for combination
   */
  private generateSku(combination: Record<string, string>): string {
    const values = Object.values(combination).join('-');
    return `${this.baseSku}-${values}`.toUpperCase();
  }

  /**
   * Gets attribute names for display
   */
  getAttributeNames(): string[] {
    return this.attributes
      .filter(attr => attr.name && attr.values.length > 0)
      .map(attr => attr.name);
  }

  /**
   * Deletes variant row from preview
   */
  deleteVariantRow(index: number): void {
    this.generatedVariants.splice(index, 1);
  }
}
