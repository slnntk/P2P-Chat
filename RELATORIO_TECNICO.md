# Relatório Técnico - Sistema de Chat P2P

## Arquitetura do Sistema

### Visão Geral
O sistema implementado é uma solução de chat descentralizada baseada no protocolo P2P (peer-to-peer), desenvolvida em Java utilizando sockets TCP/IP. Cada peer atua simultaneamente como cliente e servidor, permitindo comunicação direta entre os usuários sem necessidade de servidor central.

### Componentes Principais

#### 1. Classe Peer
- **Responsabilidade**: Gerenciar múltiplas conexões P2P e coordenar a transmissão de mensagens
- **Funcionalidades**:
  - Aceitar conexões de entrada (servidor)
  - Estabelecer conexões com outros peers (cliente)
  - Gerenciar lista de conexões ativas
  - Implementar broadcasting de mensagens
  - Controlar ciclo de vida das conexões

#### 2. Classe PeerConnection
- **Responsabilidade**: Representar uma conexão individual com outro peer
- **Funcionalidades**:
  - Comunicação bidirecional via socket TCP
  - Handshake para identificação de peers
  - Processamento de mensagens em thread separada
  - Gerenciamento do estado da conexão
  - Tratamento de desconexões

#### 3. Classe Message
- **Responsabilidade**: Estrutura de dados para mensagens do chat
- **Características**:
  - Informações do remetente
  - Conteúdo da mensagem
  - Timestamp automático
  - Serialização para transmissão em rede
  - Formatação para exibição

#### 4. Classe MessageHistory
- **Responsabilidade**: Gerenciar histórico de mensagens da sessão
- **Funcionalidades**:
  - Armazenamento thread-safe usando CopyOnWriteArrayList
  - Limitação automática de mensagens (máx. 1000)
  - Recuperação de mensagens recentes
  - Exibição formatada do histórico

#### 5. Classe PeerDiscovery
- **Responsabilidade**: Descoberta automática de peers na rede
- **Estratégias**:
  - Varredura de portas locais (localhost)
  - Opcional: varredura de subnet local
  - Execução assíncrona com timeouts
  - Lista de portas comuns configurável

#### 6. Classe ChatClient
- **Responsabilidade**: Interface de usuário console
- **Funcionalidades**:
  - Menu interativo de comandos
  - Gerenciamento do ciclo de vida da aplicação
  - Processamento de entrada do usuário
  - Tratamento de shutdown gracioso

## Decisões Técnicas

### 1. Protocolo de Comunicação
**Decisão**: Protocolo textual simples sobre TCP
**Justificativa**:
- Simplicidade de implementação e debug
- Confiabilidade do TCP para entrega de mensagens
- Facilidade de extensão futura

**Formato**:
- `HANDSHAKE:<nome_usuario>` - Identificação inicial
- `MESSAGE:<remetente>|<conteudo>|<timestamp>` - Mensagem de chat

### 2. Modelo de Threading
**Decisão**: Thread separada para cada conexão
**Justificativa**:
- Isolamento de falhas entre conexões
- Processamento concorrente de mensagens
- Responsividade da interface do usuário
- Simplicidade de implementação

### 3. Broadcasting de Mensagens
**Decisão**: Encaminhamento por todos os peers conectados
**Justificativa**:
- Garantia de entrega para toda a rede
- Redundância natural
- Prevenção de loops (não reenvio para remetente)

### 4. Descoberta de Peers
**Decisão**: Varredura de portas em faixas conhecidas
**Justificativa**:
- Simplicidade de implementação
- Eficaz para redes locais
- Não requer infraestrutura adicional
- Timeout configurável para eficiência

### 5. Estrutura de Dados Thread-Safe
**Decisão**: Uso de CopyOnWriteArrayList e AtomicBoolean
**Justificativa**:
- Segurança em ambiente multi-thread
- Performance adequada para o caso de uso
- Consistência de dados garantida

## Dificuldades Encontradas

### 1. Gerenciamento de Concorrência
**Problema**: Coordenação entre múltiplas threads
**Solução**: 
- Uso de estruturas thread-safe
- Atomic variables para flags de controle
- Sincronização adequada de recursos compartilhados

### 2. Prevenção de Loops de Mensagem
**Problema**: Mensagens sendo reenviadas infinitamente
**Solução**: 
- Identificação da conexão de origem
- Não reenvio para o remetente original
- Controle de estado das conexões

### 3. Tratamento de Desconexões
**Problema**: Detecção e limpeza de conexões mortas
**Solução**:
- Monitoring de estado dos sockets
- Cleanup automático em caso de erro
- Notificação adequada aos usuários

### 4. Interface de Usuário Responsiva
**Problema**: Manter UI responsiva durante operações de rede
**Solução**:
- Threading separado para operações de rede
- Comandos não-bloqueantes
- Feedback adequado ao usuário

## Limitações Identificadas

### 1. Segurança
- Mensagens transmitidas em texto plano
- Ausência de autenticação forte
- Vulnerável a ataques de rede local

### 2. Escalabilidade
- Limitado a redes locais por padrão
- Performance degrada com muitos peers
- Não otimizado para WANs

### 3. Persistência
- Histórico perdido ao fechar aplicação
- Sem sincronização de estado entre sessões
- Configurações não persistentes

### 4. Descoberta de Peers
- Limitada a faixas de portas conhecidas
- Não funciona através de NATs
- Sem mecanismo de registro/diretório

## Melhorias Futuras Recomendadas

### 1. Segurança
- Implementar criptografia TLS/SSL
- Sistema de autenticação robusto
- Verificação de integridade de mensagens

### 2. Interface de Usuário
- GUI usando JavaFX ou Swing
- Notificações visuais
- Configurações persistentes

### 3. Funcionalidades Avançadas
- Compartilhamento de arquivos
- Salas de chat temáticas
- Status de presença dos usuários
- Histórico persistente

### 4. Descoberta e Conectividade
- Servidor de descoberta opcional
- Suporte a NAT traversal
- Conectividade via internet

### 5. Performance e Escalabilidade
- Otimização de broadcast
- Compressão de mensagens
- Load balancing de conexões

## Conclusão

O sistema desenvolvido atende satisfatoriamente aos requisitos especificados, fornecendo uma base sólida para um chat P2P funcional. A arquitetura modular facilita futuras expansões, e as decisões técnicas tomadas equilibram simplicidade com funcionalidade. As limitações identificadas são aceitáveis para o escopo atual e oferecem oportunidades claras para melhorias futuras.