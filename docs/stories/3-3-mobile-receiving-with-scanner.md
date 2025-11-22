# Story 3.3: Mobile Receiving with Barcode Scanner (Recebimento Mobile com Scanner)

**Epic**: 3 - Purchasing & Inventory Replenishment
**Story ID**: 3.3
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **estoquista**,
Eu quero **interface mobile com scanner de código de barras via câmera para receber mercadorias**,
Para que **eu possa registrar recebimentos de forma ágil diretamente no depósito usando smartphone**.

---

## Context & Business Value

Esta story implementa a interface mobile-first para recebimento de mercadorias com scanner de código de barras usando a câmera do celular (biblioteca ZXing). Permite operação com uma mão em ambiente de depósito, eliminando necessidade de equipamentos especializados.

**Valor de Negócio:**
- **Agilidade**: Recebimento rápido através de scanning de código de barras
- **Mobilidade**: Opera no celular, permite receber direto no local de armazenamento
- **Redução de Erros**: Scanner automático evita digitação manual
- **Custo**: Usa câmera do celular, não necessita scanner dedicado

**Contexto Arquitetural:**
- **Mobile-First**: PWA otimizado para smartphone (375x667+ resolução)
- **Camera API**: Acesso à câmera nativa via WebRTC ou Capacitor
- **ZXing**: Biblioteca open-source para decodificação de códigos de barras
- **Offline-capable** (Fase 2): Service Worker para operar sem conexão
- **Story 3.4 processa**: Esta story apenas captura dados, Story 3.4 processa e atualiza estoque

---

## Acceptance Criteria

### AC1: PWA Mobile Configurado
- [ ] Aplicação Angular configurada como PWA (manifest.json, service worker)
- [ ] Ícone de app instalável no smartphone
- [ ] Theme color configurado (Material theme)
- [ ] Viewport configurado para mobile (width=device-width, initial-scale=1)
- [ ] Suporte a modo portrait (vertical)

### AC2: Biblioteca ZXing Integrada
- [ ] Biblioteca `@zxing/ngx-scanner` ou `@zxing/browser` instalada
- [ ] Component `BarcodeScannerComponent` criado
- [ ] Scanner acessa câmera traseira do dispositivo (preferencial)
- [ ] Scanner detecta códigos: EAN-13, EAN-8, Code128, QR Code
- [ ] Reconhecimento de código em < 2 segundos (NFR8)
- [ ] Feedback visual quando código é detectado (beep sonoro + vibração)
- [ ] Permissão de câmera solicitada ao usuário

### AC3: Endpoint de Listagem de OCs Pendentes
- [ ] `GET /api/purchase-orders/pending-receipt` retorna OCs com status SENT ou PARTIALLY_RECEIVED
- [ ] Response inclui: order_number, supplier_name, order_date, items_summary (total itens, total recebido)
- [ ] Filtro opcional: `supplier_id`
- [ ] Ordenação: por order_date decrescente (mais recentes primeiro)

### AC4: Endpoint de Detalhes de OC para Recebimento
- [ ] `GET /api/purchase-orders/{id}/receiving-details` retorna detalhes para recebimento:
  ```json
  {
    "id": "uuid",
    "order_number": "PO-202511-0042",
    "supplier_name": "Distribuidora ABC",
    "stock_location_name": "Depósito Central",
    "items": [
      {
        "id": "uuid",
        "product_id": "uuid",
        "product_name": "Notebook Dell Inspiron 15",
        "sku": "PROD-001",
        "barcode": "7891234567890",
        "quantity_ordered": 100.00,
        "quantity_received": 50.00,
        "quantity_pending": 50.00,
        "unit_cost": 15.50
      }
    ]
  }
  ```
- [ ] Campo `quantity_pending` calculado: `quantity_ordered - quantity_received`
- [ ] Retorna HTTP 404 se OC não encontrada

### AC5: Frontend Mobile - Seleção de OC
- [ ] Component `ReceivingOrderSelectionComponent` criado
- [ ] Lista de OCs pendentes de recebimento (cards mobile-friendly)
- [ ] Cada card exibe: order_number, fornecedor, data, progresso (X de Y itens recebidos)
- [ ] Barra de progresso visual por OC (% itens recebidos)
- [ ] Botão "Receber" abre tela de scanning
- [ ] Pull-to-refresh para atualizar lista

### AC6: Frontend Mobile - Tela de Scanning
- [ ] Component `BarcodeScanningComponent` criado
- [ ] View fullscreen com preview da câmera (60-70% da tela)
- [ ] Overlay com área de foco (quadrado/retângulo indicando onde escanear)
- [ ] Botão "Alternar Câmera" (frontal/traseira)
- [ ] Botão "Entrada Manual" (fallback se scanner não funcionar)
- [ ] Header exibe: ordem_number, fornecedor
- [ ] Footer exibe quantidade de itens já escaneados na sessão

### AC7: Frontend Mobile - Reconhecimento e Feedback
- [ ] Ao detectar código de barras:
  1. Emitir beep sonoro (opcional, configurável)
  2. Vibração haptic (100ms)
  3. Flash visual verde no overlay
  4. Exibir toast com produto identificado
- [ ] Se código não encontrado nos itens da OC:
  - Vibração de erro (padrão diferente)
  - Toast vermelho: "Produto não encontrado nesta ordem"
  - Continuar scanning
- [ ] Se código encontrado, abrir modal de confirmação de quantidade

### AC8: Frontend Mobile - Modal de Confirmação de Quantidade
- [ ] Modal exibe:
  - Nome do produto
  - SKU / Barcode escaneado
  - Quantidade pendente: X unidades
  - Input numérico: "Quantidade recebida"* (default: quantidade pendente ou 1)
  - Teclado numérico otimizado para mobile
- [ ] Validação: quantidade <= quantidade pendente
- [ ] Botões: "Confirmar" (verde), "Cancelar" (cinza)
- [ ] Ao confirmar, adiciona item à "fila de recebimento local" (não salva ainda)
- [ ] Retorna ao scanner automaticamente após confirmar

### AC9: Frontend Mobile - Resumo de Recebimento Parcial
- [ ] Botão flutuante "Ver Resumo" (badge com contador de itens)
- [ ] Tela de resumo lista itens escaneados na sessão:
  - Produto (SKU - Nome)
  - Quantidade a receber
  - Botão "Remover" inline
- [ ] Total de itens e valor total exibidos no footer
- [ ] Botões: "Finalizar Recebimento" (confirma tudo), "Continuar Escaneando" (volta ao scanner)

### AC10: Entrada Manual (Fallback)
- [ ] Botão "Entrada Manual" abre modal com:
  - Busca de produto (autocomplete por nome ou SKU)
  - Quantidade* (input numérico)
  - Botão "Adicionar"
- [ ] Validação: produto deve estar na OC selecionada
- [ ] Validação: quantidade <= quantidade pendente
- [ ] Adiciona à fila de recebimento local
- [ ] Retorna ao scanner

---

## Tasks & Subtasks

### Task 1: Configurar PWA no Angular
- [ ] Instalar `@angular/pwa`: `ng add @angular/pwa`
- [ ] Configurar `manifest.json` (name, icons, theme_color)
- [ ] Configurar service worker básico (ngsw-config.json)
- [ ] Testar instalação do app no smartphone

### Task 2: Integrar Biblioteca ZXing
- [ ] Instalar `@zxing/ngx-scanner` ou `@zxing/browser`
- [ ] Criar `BarcodeScannerComponent`
- [ ] Implementar acesso à câmera (WebRTC ou Capacitor Camera)
- [ ] Configurar formatos de código suportados
- [ ] Implementar callback ao detectar código
- [ ] Testar reconhecimento com códigos de barras reais

### Task 3: Criar Endpoints de Recebimento (Backend)
- [ ] Criar `GET /api/purchase-orders/pending-receipt`
- [ ] Criar `GET /api/purchase-orders/{id}/receiving-details`
- [ ] DTOs: `ReceivingOrderSummaryDTO`, `ReceivingOrderDetailDTO`, `ReceivingItemDTO`
- [ ] Lógica de cálculo de `quantity_pending`

### Task 4: Frontend - ReceivingOrderSelectionComponent
- [ ] Criar component com lista de OCs pendentes
- [ ] Design mobile-first com cards
- [ ] Barra de progresso por OC
- [ ] Pull-to-refresh (usando lib ou customizado)
- [ ] Service: `ReceivingService.getPendingOrders()`

### Task 5: Frontend - BarcodeScanningComponent
- [ ] Criar component com preview de câmera fullscreen
- [ ] Overlay com área de foco
- [ ] Botões: Alternar Câmera, Entrada Manual
- [ ] Integração com BarcodeScannerComponent
- [ ] Feedback visual/sonoro ao detectar código

### Task 6: Frontend - Modal de Confirmação de Quantidade
- [ ] Criar component `ReceivingQuantityModalComponent`
- [ ] Input numérico otimizado para mobile (type="number", inputmode="decimal")
- [ ] Validação de quantidade máxima
- [ ] Gerenciamento de "fila local" de itens a receber (RxJS BehaviorSubject)

### Task 7: Frontend - Resumo de Recebimento
- [ ] Criar component `ReceivingSummaryComponent`
- [ ] Lista de itens a receber (em memória, ainda não salvos)
- [ ] Cálculo de totais (quantidade, valor)
- [ ] Botão "Finalizar" prepara payload para Story 3.4

### Task 8: Frontend - Entrada Manual Fallback
- [ ] Modal de entrada manual com busca de produto
- [ ] Autocomplete com debounce
- [ ] Validações inline

### Task 9: Testes

#### Testing

- [ ] Teste: PWA é instalável no smartphone
- [ ] Teste: Scanner detecta código EAN-13 em < 2s
- [ ] Teste: Permissão de câmera é solicitada
- [ ] Teste: Endpoint pending-receipt retorna OCs corretas
- [ ] Teste: Quantidade pendente calculada corretamente
- [ ] Teste: Feedback haptic funciona (testar em device real)

---

## Definition of Done (DoD)

- [ ] PWA configurado e instalável no smartphone
- [ ] Biblioteca ZXing integrada e funcional
- [ ] Endpoints de listagem de OCs pendentes criados
- [ ] Frontend mobile lista OCs pendentes com progresso
- [ ] Scanner de código de barras funciona via câmera
- [ ] Feedback visual/sonoro ao detectar código implementado
- [ ] Modal de confirmação de quantidade funciona
- [ ] Resumo de recebimento parcial exibe itens escaneados
- [ ] Entrada manual (fallback) funciona
- [ ] Testado em dispositivo mobile real (Android/iOS)
- [ ] Performance: reconhecimento < 2s (NFR8)
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 3.2 (Purchase Order Creation) - Precisa de OCs cadastradas para receber
- Story 2.2 (Simple Products) - Produtos precisam ter barcode cadastrado

**Bloqueia:**
- Story 3.4 (Processamento de Recebimento) - 3.4 processa o payload gerado por 3.3

**Observação:**
Esta story foca na **captura de dados** (UX mobile + scanning). A Story 3.4 implementará o **processamento do recebimento** (atualização de estoque, custo médio ponderado, movimentações).

---

## Technical Notes

**Integração ZXing (Angular):**
```typescript
// barcode-scanner.component.ts
import { BarcodeFormat } from '@zxing/library';

export class BarcodeScannerComponent {
  allowedFormats = [
    BarcodeFormat.EAN_13,
    BarcodeFormat.EAN_8,
    BarcodeFormat.CODE_128,
    BarcodeFormat.QR_CODE
  ];

  hasDevices: boolean;
  hasPermission: boolean;
  availableDevices: MediaDeviceInfo[];
  currentDevice: MediaDeviceInfo;

  onCodeResult(resultString: string) {
    // Vibração haptic
    if (navigator.vibrate) {
      navigator.vibrate(100);
    }

    // Emitir beep (opcional)
    this.playBeep();

    // Callback para componente pai
    this.barcodeDetected.emit(resultString);
  }

  onHasPermission(has: boolean) {
    this.hasPermission = has;
    if (!has) {
      this.showPermissionDeniedMessage();
    }
  }

  playBeep() {
    const audioContext = new AudioContext();
    const oscillator = audioContext.createOscillator();
    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
    oscillator.connect(audioContext.destination);
    oscillator.start();
    oscillator.stop(audioContext.currentTime + 0.1);
  }
}
```

**Template do Scanner:**
```html
<!-- barcode-scanning.component.html -->
<div class="scanning-container">
  <header class="mobile-header">
    <h2>{{ orderNumber }}</h2>
    <p>{{ supplierName }}</p>
  </header>

  <div class="camera-preview">
    <zxing-scanner
      [formats]="allowedFormats"
      [device]="currentDevice"
      (scanSuccess)="onCodeResult($event)"
      (permissionResponse)="onHasPermission($event)"
      (camerasFound)="onCamerasFound($event)">
    </zxing-scanner>

    <div class="scan-overlay">
      <div class="scan-area"></div>
      <p>Posicione o código de barras na área</p>
    </div>
  </div>

  <div class="actions">
    <button mat-raised-button (click)="toggleCamera()">
      <mat-icon>switch_camera</mat-icon>
      Alternar Câmera
    </button>
    <button mat-raised-button (click)="openManualEntry()">
      <mat-icon>keyboard</mat-icon>
      Entrada Manual
    </button>
  </div>

  <footer class="mobile-footer">
    <p>{{ scannedItemsCount }} itens escaneados</p>
    <button mat-fab color="primary" (click)="openSummary()" *ngIf="scannedItemsCount > 0">
      <mat-icon [matBadge]="scannedItemsCount">shopping_cart</mat-icon>
    </button>
  </footer>
</div>
```

**Service de Gerenciamento de Fila Local:**
```typescript
// receiving.service.ts
export interface ReceivingItem {
  purchaseOrderItemId: string;
  productId: string;
  productName: string;
  sku: string;
  barcode: string;
  quantityToReceive: number;
  unitCost: number;
}

@Injectable({ providedIn: 'root' })
export class ReceivingService {
  private receivingQueue$ = new BehaviorSubject<ReceivingItem[]>([]);

  addItem(item: ReceivingItem) {
    const current = this.receivingQueue$.value;
    // Verificar se item já existe, se sim, somar quantidade
    const existingIndex = current.findIndex(i => i.purchaseOrderItemId === item.purchaseOrderItemId);

    if (existingIndex >= 0) {
      current[existingIndex].quantityToReceive += item.quantityToReceive;
    } else {
      current.push(item);
    }

    this.receivingQueue$.next([...current]);
  }

  removeItem(purchaseOrderItemId: string) {
    const current = this.receivingQueue$.value.filter(
      i => i.purchaseOrderItemId !== purchaseOrderItemId
    );
    this.receivingQueue$.next(current);
  }

  getQueue(): Observable<ReceivingItem[]> {
    return this.receivingQueue$.asObservable();
  }

  clearQueue() {
    this.receivingQueue$.next([]);
  }

  getTotalValue(): number {
    return this.receivingQueue$.value.reduce(
      (sum, item) => sum + (item.quantityToReceive * item.unitCost), 0
    );
  }
}
```

**Manifest PWA (manifest.json):**
```json
{
  "name": "Estoque Central - Recebimento",
  "short_name": "Recebimento",
  "theme_color": "#1976d2",
  "background_color": "#fafafa",
  "display": "standalone",
  "scope": "/",
  "start_url": "/receiving",
  "icons": [
    {
      "src": "assets/icons/icon-72x72.png",
      "sizes": "72x72",
      "type": "image/png"
    },
    {
      "src": "assets/icons/icon-96x96.png",
      "sizes": "96x96",
      "type": "image/png"
    },
    {
      "src": "assets/icons/icon-128x128.png",
      "sizes": "128x128",
      "type": "image/png"
    },
    {
      "src": "assets/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "assets/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Adicionadas seções Status, Testing, QA Results (template compliance) |

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
- Outstanding ZXing integration design
- PWA setup comprehensive
- NFR8 compliance (< 2s barcode recognition) well-specified
- Mobile-first UX properly designed
- Ready for development

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 3, docs/epics/epic-03-purchasing.md
