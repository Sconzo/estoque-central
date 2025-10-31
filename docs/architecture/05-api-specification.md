# 5. API Specification

## 5.1. OpenAPI 3.0 Spec

```yaml
openapi: 3.0.0
info:
  title: Estoque Central API
  version: 1.0.0
  description: API REST para sistema ERP omnichannel

servers:
  - url: http://localhost:8080/api
    description: Local development
  - url: https://api.estoquecentral.com
    description: Production

security:
  - BearerAuth: []
  - TenantHeader: []

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
    TenantHeader:
      type: apiKey
      in: header
      name: X-Tenant-ID

  schemas:
    Money:
      type: object
      properties:
        valor:
          type: integer
          format: int64
          description: Valor em centavos
        moeda:
          type: string
          enum: [BRL]
      required: [valor, moeda]

    Produto:
      type: object
      properties:
        id:
          type: string
          format: uuid
        tipo:
          type: string
          enum: [SIMPLES, VARIANTE_PAI, VARIANTE_FILHO, COMPOSTO]
        sku:
          type: string
        nome:
          type: string
        preco:
          $ref: '#/components/schemas/Money'
        custo:
          $ref: '#/components/schemas/Money'
        categoriaId:
          type: string
          format: uuid
        ativo:
          type: boolean

    Venda:
      type: object
      properties:
        id:
          type: string
          format: uuid
        numero:
          type: string
        tipo:
          type: string
          enum: [PDV, B2B, B2C, MERCADO_LIVRE]
        status:
          type: string
          enum: [PENDENTE, PAGO, CANCELADA]
        total:
          $ref: '#/components/schemas/Money'

paths:
  # Produtos
  /produtos:
    get:
      tags: [Produtos]
      summary: Listar produtos
      parameters:
        - name: page
          in: query
          schema:
            type: integer
        - name: size
          in: query
          schema:
            type: integer
        - name: search
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Lista paginada de produtos

    post:
      tags: [Produtos]
      summary: Criar produto
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Produto'
      responses:
        '201':
          description: Produto criado

  /produtos/{id}:
    get:
      tags: [Produtos]
      summary: Buscar produto por ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Produto encontrado
        '404':
          description: Produto não encontrado

    put:
      tags: [Produtos]
      summary: Atualizar produto
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Produto'
      responses:
        '200':
          description: Produto atualizado

    delete:
      tags: [Produtos]
      summary: Desativar produto
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '204':
          description: Produto desativado

  # Vendas
  /vendas:
    get:
      tags: [Vendas]
      summary: Listar vendas
      responses:
        '200':
          description: Lista de vendas

    post:
      tags: [Vendas]
      summary: Criar venda
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Venda'
      responses:
        '201':
          description: Venda criada

  /vendas/{id}/finalizar:
    post:
      tags: [Vendas]
      summary: Finalizar venda e emitir NFCe
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                formaPagamento:
                  type: string
                  enum: [DINHEIRO, CARTAO_CREDITO, PIX]
      responses:
        '200':
          description: Venda finalizada

  /vendas/{id}/cancelar:
    post:
      tags: [Vendas]
      summary: Cancelar venda
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                motivo:
                  type: string
      responses:
        '200':
          description: Venda cancelada

  # Estoque
  /estoque:
    get:
      tags: [Estoque]
      summary: Listar estoque
      responses:
        '200':
          description: Lista de estoque

  /estoque/movimentacoes:
    post:
      tags: [Estoque]
      summary: Registrar movimentação
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                produtoId:
                  type: string
                  format: uuid
                tipo:
                  type: string
                  enum: [ENTRADA_COMPRA, SAIDA_VENDA, AJUSTE_ENTRADA]
                quantidade:
                  type: integer
      responses:
        '201':
          description: Movimentação registrada

  # Integrações Mercado Livre
  /integracoes/mercadolivre/produtos/{produtoId}/sync:
    post:
      tags: [Integracoes]
      summary: Sincronizar produto com ML
      parameters:
        - name: produtoId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Produto sincronizado

  /integracoes/mercadolivre/pedidos/import:
    post:
      tags: [Integracoes]
      summary: Importar pedidos do ML
      responses:
        '200':
          description: Pedidos importados
