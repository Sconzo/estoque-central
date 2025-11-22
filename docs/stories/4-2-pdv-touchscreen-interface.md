# Story 4.2: PDV Touchscreen Interface

**Epic**: 4 - Sales Channels - PDV & B2B
**Story ID**: 4.2
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **operador de caixa**,
Eu quero **interface PDV otimizada para tablet touchscreen com fluxo scan-to-pay em 3-5 toques**,
Para que **eu possa processar vendas rapidamente com mínima interação**.

---

## Context & Business Value

Interface PDV fullscreen otimizada para tablet (1280x800), com suporte a leitor de código de barras USB/Bluetooth, busca rápida de produtos e checkout em menos de 30 segundos (NFR2).

**Valor de Negócio:**
- **Velocidade**: Checkout completo em < 30s para transação típica 5 itens (NFR2)
- **Usabilidade**: Interface touchscreen com targets mínimos 44x44px
- **Produtividade**: 3-5 toques do scan ao pagamento
- **Performance**: 60fps para responsividade (NFR7)

---

## Acceptance Criteria

### AC1: Layout Fullscreen Otimizado para Tablet
- [ ] Interface ocupa tela completa (F11 ou modo kiosk)
- [ ] Layout 3 colunas: Produtos (40%), Carrinho (35%), Ações (25%)
- [ ] Touch targets mínimos 44x44px (WCAG AA)
- [ ] Fonte mínima 16px para legibilidade
- [ ] Performance: 60fps (NFR7)

### AC2: Busca e Adição de Produtos
- [ ] Input de busca com autofocus (sempre ativo para scanner)
- [ ] Scanner USB/Bluetooth: detecta código de barras e adiciona automaticamente
- [ ] Busca manual: autocomplete por nome ou SKU (debounce 200ms)
- [ ] Grid de produtos recentes/mais vendidos (quick access)
- [ ] Ao adicionar produto: exibe toast confirmação e limpa input
- [ ] Busca retorna resultados em < 500ms (NFR3)

### AC3: Carrinho de Compras
- [ ] Lista de itens com colunas: Produto, Qtd, Preço Unit., Subtotal, Remover
- [ ] Edição de quantidade inline (teclado numérico virtual)
- [ ] Botão remover item (ícone X, target 44x44px)
- [ ] Exibe subtotal, descontos (futura), total em destaque
- [ ] Carrinho vazio exibe mensagem "Escaneie ou busque produtos"

### AC4: Seleção de Cliente (Opcional)
- [ ] Botão "Cliente" abre modal de busca rápida
- [ ] Autocomplete por CPF, CNPJ ou nome
- [ ] Botão "Cadastro Rápido" (Story 4.1)
- [ ] Padrão: "Consumidor Final" se não informado
- [ ] Cliente selecionado exibe nome no header do PDV

### AC5: Finalização e Pagamento
- [ ] Botão "Finalizar Venda" (verde, destaque, desabilitado se carrinho vazio)
- [ ] Modal de pagamento: Dinheiro, Débito, Crédito, PIX (futura)
- [ ] Input valor recebido (se Dinheiro)
- [ ] Cálculo automático de troco
- [ ] Teclado numérico virtual otimizado para touch
- [ ] Botão "Confirmar Pagamento" processa venda (Story 4.3)

### AC6: Feedback Visual e Loading States
- [ ] Loading spinner durante processamento de venda
- [ ] Toast de sucesso: "Venda realizada! NFCe: [chave]"
- [ ] Toast de erro: mensagem clara do backend
- [ ] Skeleton screens ao carregar produtos
- [ ] Feedback haptic ao adicionar produto (se tablet suportar)

### AC7: Atalhos de Teclado
- [ ] F9: Finalizar venda
- [ ] F2: Buscar cliente
- [ ] F5: Limpar carrinho (com confirmação)
- [ ] Esc: Cancelar operação atual
- [ ] Enter no input de busca: adiciona primeiro resultado

---

## Tasks & Subtasks

### Task 1: Criar PDVLayoutComponent
- [ ] Layout fullscreen com 3 colunas responsivas
- [ ] Header: logo, nome loja, operador, data/hora
- [ ] Footer: totalizadores em destaque

### Task 2: Criar ProductSearchComponent
- [ ] Input com autofocus e detecção de scanner
- [ ] Autocomplete com Angular Material
- [ ] Grid de produtos favoritos
- [ ] Service: `ProductService.searchForPDV(query)`

### Task 3: Criar ShoppingCartComponent
- [ ] Lista de itens com edição inline
- [ ] Gerenciamento de estado com BehaviorSubject
- [ ] Cálculo automático de totais
- [ ] Validação: quantidade > 0

### Task 4: Criar CustomerSelectionModalComponent
- [ ] Modal com busca rápida (reusa Story 4.1)
- [ ] Integração com CustomerQuickSearchComponent

### Task 5: Criar PaymentModalComponent
- [ ] Seleção de forma de pagamento (radio buttons grandes)
- [ ] Teclado numérico virtual (component reutilizável)
- [ ] Cálculo de troco para Dinheiro
- [ ] Validação: valor recebido >= total

### Task 6: Implementar Atalhos de Teclado
- [ ] Service `KeyboardShortcutsService` com HostListener
- [ ] Tooltip indicando atalhos disponíveis

### Task 7: Otimizações de Performance
- [ ] OnPush change detection strategy
- [ ] Virtual scrolling para lista de produtos (se > 50 itens)
- [ ] Debounce em buscas
- [ ] Lazy loading de imagens

### Task 8: Testes E2E

#### Testing

- [ ] Teste: adicionar produto via busca
- [ ] Teste: adicionar produto via scanner (simular input)
- [ ] Teste: editar quantidade no carrinho
- [ ] Teste: remover item do carrinho
- [ ] Teste: finalizar venda com Consumidor Final
- [ ] Teste: finalizar venda com cliente específico
- [ ] Teste: performance 60fps (Lighthouse)

---

## Definition of Done (DoD)

- [ ] Interface fullscreen otimizada para tablet
- [ ] Suporte a scanner USB/Bluetooth
- [ ] Busca de produtos em < 500ms (NFR3)
- [ ] Carrinho de compras funcional
- [ ] Seleção de cliente (opcional)
- [ ] Modal de pagamento implementado
- [ ] Atalhos de teclado funcionam
- [ ] Performance: 60fps (NFR7)
- [ ] Checkout completo em < 30s para 5 itens (NFR2)
- [ ] Touch targets mínimos 44x44px (WCAG AA)
- [ ] Testes E2E passando
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Depende de:**
- Story 4.1 (Customer Management) - Busca de clientes
- Story 2.2 (Simple Products) - Produtos com barcode

**Bloqueia:**
- Story 4.3 (Emissão NFCe) - PDV prepara payload de venda

---

## Technical Notes

**Detecção de Scanner de Código de Barras:**
```typescript
@Component({
  selector: 'app-product-search',
  template: `<input #searchInput (input)="onInput($event)" />`
})
export class ProductSearchComponent implements OnInit {
  @ViewChild('searchInput') searchInput: ElementRef;
  private scanBuffer = '';
  private scanTimeout: any;

  ngOnInit() {
    // Scanner envia caracteres rapidamente (<100ms entre chars)
    // Teclado manual tem delay > 100ms
  }

  onInput(event: Event) {
    const value = (event.target as HTMLInputElement).value;

    clearTimeout(this.scanTimeout);

    this.scanBuffer += value;

    this.scanTimeout = setTimeout(() => {
      if (this.scanBuffer.length >= 8) {
        // Provavelmente scanner (código de barras típico 13 dígitos)
        this.onBarcodeScanned(this.scanBuffer);
        this.searchInput.nativeElement.value = '';
      }
      this.scanBuffer = '';
    }, 100);
  }

  onBarcodeScanned(barcode: string) {
    this.productService.findByBarcode(barcode).subscribe(product => {
      if (product) {
        this.addToCart(product);
        this.showToast('Produto adicionado!');
      } else {
        this.showToast('Produto não encontrado', 'error');
      }
    });
  }
}
```

**Payload Preparado pelo PDV (para Story 4.3):**
```typescript
export interface SaleRequest {
  customer_id?: string; // UUID ou null (Consumidor Final)
  stock_location_id: string; // Local de venda (loja)
  payment_method: 'DINHEIRO' | 'DEBITO' | 'CREDITO' | 'PIX';
  payment_amount_received?: number; // Se Dinheiro
  items: SaleItemRequest[];
}

export interface SaleItemRequest {
  product_id?: string;
  variant_id?: string;
  quantity: number;
  unit_price: number; // Preço do produto no momento da venda
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Adicionadas seções Status, Testing, Dev Agent Record, QA Results (template compliance) |

---

## Dev Agent Record

**Agent Model Used:**
Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

---

## QA Results

**Validation Status**: ✅ Approved
**Validated By**: Sarah (Product Owner)
**Validation Date**: 2025-11-21

**Findings**:
- ✅ Epic alignment verified
- ✅ Acceptance Criteria complete and testable
- ✅ Task breakdown adequate
- ✅ No migration conflicts (frontend-only story)
- ✅ Template compliance achieved

**Notes**:
- Excellent touchscreen UX design (44x44px touch targets - WCAG AA)
- Barcode scanner detection algorithm well-designed (< 100ms timing)
- NFR compliance clearly specified (NFR2: < 30s checkout, NFR3: < 500ms search, NFR7: 60fps)
- Keyboard shortcuts enhance productivity
- Ready for development

**UX Highlights**:
- 3-column layout optimized for tablet (Produtos 40%, Carrinho 35%, Ações 25%)
- Fullscreen/kiosk mode for distraction-free operation
- Virtual numeric keyboard for touch input
- Skeleton screens for perceived performance

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 4, docs/epics/epic-04-sales-channels.md
