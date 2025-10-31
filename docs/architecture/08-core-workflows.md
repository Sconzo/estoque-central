# 8. Core Workflows

## 8.1. Fluxo de Autenticação OAuth

```mermaid
sequenceDiagram
    participant U as Usuário
    participant F as Frontend
    participant B as Backend
    participant G as Google OAuth

    U->>F: Clicar "Login com Google"
    F->>G: Redirecionar para Google
    G->>U: Tela de login
    U->>G: Credenciais
    G->>F: Callback com code
    F->>B: POST /auth/google/callback {code}
    B->>G: Trocar code por tokens
    G-->>B: Access token + ID token
    B->>B: Validar token
    B->>B: Criar/atualizar usuário
    B->>B: Gerar JWT customizado
    B-->>F: JWT + user data
    F->>F: Armazenar JWT no localStorage
    F-->>U: Redirecionar para dashboard
```

## 8.2. Fluxo de Venda PDV com NFCe Retry

```mermaid
sequenceDiagram
    participant O as Operador
    participant PDV as Frontend PDV
    participant API as Backend API
    participant Est as Estoque Module
    participant Fiscal as Fiscal Module
    participant Queue as Redis DelayedQueue
    participant NFCe as Focus NFe

    O->>PDV: Adicionar itens ao carrinho
    PDV->>API: POST /vendas {itens, cliente}
    API->>Est: Reservar estoque
    Est-->>API: Estoque reservado
    API-->>PDV: Venda criada (PENDENTE)

    O->>PDV: Finalizar pagamento
    PDV->>API: POST /vendas/:id/finalizar {formaPagamento}
    API->>Est: Baixar estoque reservado
    API->>Fiscal: Emitir NFCe
    Fiscal->>NFCe: POST /v2/nfce

    alt NFCe emitida com sucesso
        NFCe-->>Fiscal: Autorizada {chave}
        Fiscal-->>API: NFCe autorizada
        API-->>PDV: Venda finalizada com NFCe
    else NFCe falha
        NFCe-->>Fiscal: Erro (timeout, sefaz offline)
        Fiscal->>Queue: Agendar retry (tentativa 1, delay 30s)
        Fiscal-->>API: NFCe pendente
        API-->>PDV: Venda finalizada (NFCe em processamento)

        Note over Queue: Após 30s
        Queue->>Fiscal: Retry tentativa 1
        Fiscal->>NFCe: POST /v2/nfce

        alt Retry sucesso
            NFCe-->>Fiscal: Autorizada
        else Retry falha
            Fiscal->>Queue: Agendar retry (tentativa 2, delay 1min)
        end
    end
```

## 8.3. Fluxo de Importação de Pedido Mercado Livre

```mermaid
sequenceDiagram
    participant Scheduler as Cron Job
    participant ML as ML Module
    participant API as ML API
    participant Vendas as Vendas Module
    participant Est as Estoque Module

    Scheduler->>ML: Executar import (a cada 5min)
    ML->>API: GET /orders/search?seller=X&offset=0
    API-->>ML: Lista de pedidos

    loop Para cada pedido novo
        ML->>ML: Mapear pedido ML -> Venda
        ML->>Vendas: Criar venda (tipo=MERCADO_LIVRE)
        Vendas->>Est: Reservar estoque
        Est-->>Vendas: Estoque reservado
        Vendas-->>ML: Venda criada
        ML->>API: POST /shipments/:id/ready_to_ship
    end

    ML-->>Scheduler: Import concluído
```

## 8.4. Fluxo de Recebimento de Compra

```mermaid
sequenceDiagram
    participant E as Estoquista
    participant Front as Frontend
    participant API as Backend
    participant Compras as Compras Module
    participant Est as Estoque Module

    E->>Front: Registrar recebimento
    Front->>API: POST /compras/:id/recebimentos {itens}
    API->>Compras: Validar recebimento

    loop Para cada item recebido
        Compras->>Est: Entrada de estoque
        Est->>Est: Calcular custo médio ponderado
        Note over Est: CMP = (valorAtual * qtdAtual + valorNovo * qtdNova) / (qtdAtual + qtdNova)
        Est-->>Compras: Estoque atualizado
    end

    Compras->>Compras: Atualizar status compra
    Compras-->>API: Recebimento registrado
    API-->>Front: Sucesso
```

## 8.5. Fluxo de Cancelamento de Venda

```mermaid
sequenceDiagram
    participant G as Gerente
    participant Front as Frontend
    participant API as Backend
    participant Vendas as Vendas Module
    participant Est as Estoque Module
    participant Fiscal as Fiscal Module
    participant NFCe as Focus NFe

    G->>Front: Cancelar venda
    Front->>API: POST /vendas/:id/cancelar {motivo}
    API->>Vendas: Validar cancelamento

    alt Venda tem NFCe autorizada
        Vendas->>Fiscal: Cancelar NFCe
        Fiscal->>NFCe: DELETE /v2/nfce/:ref {justificativa}
        NFCe-->>Fiscal: NFCe cancelada
    end

    Vendas->>Est: Reverter baixa de estoque
    Est-->>Vendas: Estoque devolvido
    Vendas->>Vendas: Status = CANCELADA
    Vendas-->>API: Venda cancelada
    API-->>Front: Sucesso
```
