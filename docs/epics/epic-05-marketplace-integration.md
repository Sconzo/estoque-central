# Epic 5: Marketplace Integration - Mercado Livre

**Objetivo:** Integrar com Mercado Livre via OAuth2 estabelecendo sincronização bidirecional completa de produtos (incluindo variantes), estoque e pedidos. Implementar processamento automático de pedidos e cancelamentos, margem de segurança configurável para evitar overselling e sincronização em tempo real pós-vendas em outros canais. Este épico entrega o diferencial competitivo core do produto: verdadeiro omnichannel sem risco de overselling.

**Stories:**
- 5.1: Autenticação OAuth2 Mercado Livre (tokens, refresh automático)
- 5.2: Importação de Produtos do Mercado Livre (anúncios existentes → sistema)
- 5.3: Publicação de Produtos no Mercado Livre (sistema → anúncios ML, suporte a variantes)
- 5.4: Sincronização de Estoque (Sistema → ML após vendas em outros canais, < 5min)
- 5.5: Importação de Pedidos do Mercado Livre (processamento automático + baixa de estoque)
- 5.6: Processamento de Cancelamentos ML (estorno automático de estoque)
- 5.7: Margem de Segurança Configurável (anunciar X% do estoque real, configurável por marketplace/categoria/produto)

[Detalhes completos das stories foram apresentados anteriormente]
