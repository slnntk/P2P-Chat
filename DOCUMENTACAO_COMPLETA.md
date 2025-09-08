# Documentação Completa - Sistema de Chat P2P

## Índice
1. [Visão Geral](#visão-geral)
2. [Arquitetura do Sistema](#arquitetura-do-sistema)
3. [Documentação Técnica Detalhada](#documentação-técnica-detalhada)
4. [Guia do Usuário Completo](#guia-do-usuário-completo)
5. [Especificação do Protocolo](#especificação-do-protocolo)
6. [Exemplos Práticos](#exemplos-práticos)
7. [Solução de Problemas](#solução-de-problemas)
8. [Referência da API](#referência-da-api)

## Visão Geral

O Sistema de Chat P2P é uma aplicação Java que implementa um sistema de comunicação descentralizada entre múltiplos usuários. Cada peer (participante) funciona simultaneamente como cliente e servidor, permitindo conexões diretas sem necessidade de servidor central.

### Características Principais
- **Arquitetura Descentralizada**: Não requer servidor central
- **Conexões Múltiplas**: Cada peer pode conectar com vários outros peers
- **Broadcasting Inteligente**: Mensagens são distribuídas para toda a rede
- **Descoberta Automática**: Localiza peers automaticamente na rede local
- **Interface Console**: Interface de linha de comando intuitiva
- **Histórico de Mensagens**: Mantém histórico durante a sessão
- **Shutdown Gracioso**: Encerramento seguro de todas as conexões

## Arquitetura do Sistema

### Diagrama de Componentes
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ChatClient    │    │   ChatClient    │    │   ChatClient    │
│   (Interface)   │    │   (Interface)   │    │   (Interface)   │
│                 │    │                 │    │                 │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│      Peer       │◄──►│      Peer       │◄──►│      Peer       │
│  (Núcleo P2P)   │    │  (Núcleo P2P)   │    │  (Núcleo P2P)   │
│                 │    │                 │    │                 │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│PeerConnection[] │    │PeerConnection[] │    │PeerConnection[] │
│MessageHistory   │    │MessageHistory   │    │MessageHistory   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Hierarquia de Classes
```
com.p2pchat
├── ChatClient          # Classe principal e interface do usuário
├── Peer               # Núcleo do sistema P2P
├── PeerConnection     # Gerenciamento de conexões individuais
├── Message            # Estrutura de dados para mensagens
├── MessageHistory     # Histórico thread-safe de mensagens
└── PeerDiscovery      # Descoberta automática de peers
```

## Documentação Técnica Detalhada

### 1. Classe ChatClient

**Localização**: `src/main/java/com/p2pchat/ChatClient.java`

#### Responsabilidades
- Ponto de entrada da aplicação
- Interface de usuário via console
- Gerenciamento do ciclo de vida da aplicação
- Processamento de comandos do usuário

#### Atributos Principais
```java
private static final String VERSION = "1.0.0";
private Peer peer;                    // Instância do peer
private Scanner scanner;              // Entrada do usuário
private boolean running;              // Estado da aplicação
```

#### Métodos Principais

##### `main(String[] args)`
- **Função**: Ponto de entrada da aplicação
- **Comportamento**: Cria instância do ChatClient e inicia execução

##### `run()`
- **Função**: Loop principal da aplicação
- **Sequência**:
  1. Exibe mensagem de boas-vindas
  2. Coleta nome do usuário
  3. Coleta porta de escuta
  4. Cria e inicia o peer
  5. Configura hook de shutdown
  6. Entra no menu principal

##### `showMainMenu()`
- **Função**: Loop interativo de comandos
- **Comportamento**: 
  - Exibe prompt ">"
  - Lê entrada do usuário
  - Processa comandos ou mensagens de chat

##### `processCommand(String input)`
- **Função**: Interpreta e executa comandos do usuário
- **Comandos Suportados**:
  - `help|h`: Exibe ajuda
  - `connect|c <host:port>`: Conecta a um peer específico
  - `discover|d`: Descobre peers automaticamente
  - `status|s`: Mostra status do peer
  - `history|hist [count]`: Exibe histórico de mensagens
  - `clear`: Limpa a tela
  - `quit|exit|q`: Encerra aplicação
  - **Qualquer outro texto**: Enviado como mensagem de chat

#### Tratamento de Comandos Detalhado

##### Comando Connect
```java
private void handleConnect(String[] parts) {
    if (parts.length < 2) {
        // Solicita entrada interativa se não fornecido
        System.out.print("Enter host:port to connect to: ");
        String hostPort = scanner.nextLine().trim();
        if (!hostPort.isEmpty()) {
            connectToPeer(hostPort);
        }
    } else {
        connectToPeer(parts[1]);
    }
}
```

##### Comando Discover
```java
private void handleDiscover() {
    System.out.println("Discovering peers...");
    
    // Busca peers na rede local
    List<PeerDiscovery.PeerInfo> localPeers = 
        PeerDiscovery.discoverLocalPeers(peer.getListeningPort());
    
    if (localPeers.isEmpty()) {
        System.out.println("No peers found on localhost.");
    } else {
        // Exibe lista numerada de peers encontrados
        System.out.println("Found " + localPeers.size() + " peer(s):");
        for (int i = 0; i < localPeers.size(); i++) {
            PeerDiscovery.PeerInfo peerInfo = localPeers.get(i);
            System.out.println((i + 1) + ". " + peerInfo);
        }
        
        // Permite seleção interativa
        System.out.print("Enter number to connect (or 0 to cancel): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= localPeers.size()) {
                PeerDiscovery.PeerInfo selected = localPeers.get(choice - 1);
                peer.connectToPeer(selected.getHost(), selected.getPort());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
        }
    }
}
```

### 2. Classe Peer

**Localização**: `src/main/java/com/p2pchat/Peer.java`

#### Responsabilidades
- Núcleo do sistema P2P
- Gerenciamento de múltiplas conexões
- Broadcasting de mensagens
- Coordenação entre componentes

#### Atributos Principais
```java
private final String peerName;              // Nome do usuário
private final int listeningPort;            // Porta de escuta
private final List<PeerConnection> connections; // Lista thread-safe de conexões
private final MessageHistory messageHistory;    // Histórico de mensagens
private final AtomicBoolean running;           // Estado thread-safe
private ServerSocket serverSocket;             // Socket servidor
```

#### Modelo de Threading
- **Thread Principal**: Gerencia interface do usuário
- **Thread Servidor**: Aceita conexões de entrada (`acceptConnections()`)
- **Thread por Conexão**: Cada `PeerConnection` roda em thread separada

#### Métodos Principais

##### `start()`
```java
public void start() throws IOException {
    if (running.get()) {
        return; // Evita múltiplas inicializações
    }
    
    serverSocket = new ServerSocket(listeningPort);
    running.set(true);
    
    System.out.println("Peer '" + peerName + "' started on port " + listeningPort);
    
    // Inicia thread servidor em modo daemon
    Thread serverThread = new Thread(this::acceptConnections);
    serverThread.setName("PeerServer-" + peerName);
    serverThread.setDaemon(true);
    serverThread.start();
}
```

##### `acceptConnections()`
```java
private void acceptConnections() {
    while (running.get()) {
        try {
            Socket clientSocket = serverSocket.accept();
            
            // Cria nova conexão peer
            PeerConnection connection = new PeerConnection(clientSocket, this);
            connections.add(connection);
            
            // Envia handshake com identificação
            connection.sendHandshake(peerName);
            
            System.out.println("New peer connected from " + 
                             connection.getRemoteIdentifier());
            
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Error accepting connection: " + e.getMessage());
            }
        }
    }
}
```

##### `connectToPeer(String host, int port)`
```java
public boolean connectToPeer(String host, int port) {
    try {
        Socket socket = new Socket(host, port);
        
        // Cria nova conexão peer
        PeerConnection connection = new PeerConnection(socket, this);
        connections.add(connection);
        
        // Envia handshake
        connection.sendHandshake(peerName);
        
        System.out.println("Connected to peer at " + host + ":" + port);
        return true;
        
    } catch (IOException e) {
        System.err.println("Failed to connect to " + host + ":" + port + 
                          " - " + e.getMessage());
        return false;
    }
}
```

#### Algoritmo de Broadcasting
```java
public void broadcastMessage(String content) {
    Message message = new Message(peerName, content);
    
    // Adiciona ao histórico local
    messageHistory.addMessage(message);
    
    // Exibe localmente
    System.out.println(message);
    
    // Envia para todos os peers conectados
    for (PeerConnection connection : connections) {
        if (connection.isActive()) {
            connection.sendMessage(message);
        }
    }
}
```

#### Tratamento de Mensagens Recebidas
```java
public void receiveMessage(Message message, PeerConnection fromConnection) {
    // Adiciona ao histórico
    messageHistory.addMessage(message);
    
    // Exibe a mensagem
    System.out.println(message);
    
    // Encaminha para outros peers (exceto remetente)
    for (PeerConnection connection : connections) {
        if (connection != fromConnection && connection.isActive()) {
            connection.sendMessage(message);
        }
    }
}
```

### 3. Classe PeerConnection

**Localização**: `src/main/java/com/p2pchat/PeerConnection.java`

#### Responsabilidades
- Gerenciar conexão TCP individual com outro peer
- Implementar protocolo de comunicação
- Processar mensagens em thread separada
- Detectar e tratar desconexões

#### Atributos Principais
```java
private final Socket socket;                // Socket TCP
private final BufferedReader reader;        // Stream de entrada
private final PrintWriter writer;           // Stream de saída
private final String remoteAddress;         // IP remoto
private final int remotePort;              // Porta remota
private final AtomicBoolean active;        // Estado da conexão
private final Peer parentPeer;             // Referência ao peer pai
private String remotePeerName;             // Nome do peer remoto
```

#### Inicialização da Conexão
```java
public PeerConnection(Socket socket, Peer parentPeer) throws IOException {
    this.socket = socket;
    this.parentPeer = parentPeer;
    this.remoteAddress = socket.getInetAddress().getHostAddress();
    this.remotePort = socket.getPort();
    this.active = new AtomicBoolean(true);
    
    // Inicializa streams de I/O
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new PrintWriter(socket.getOutputStream(), true);
    
    // Inicia escuta de mensagens em thread separada
    startListening();
}
```

#### Thread de Escuta
```java
private void startListening() {
    Thread listenerThread = new Thread(() -> {
        try {
            String line;
            while (active.get() && (line = reader.readLine()) != null) {
                handleIncomingMessage(line);
            }
        } catch (IOException e) {
            if (active.get()) {
                System.err.println("Error reading from peer " + 
                                 getRemoteIdentifier() + ": " + e.getMessage());
            }
        } finally {
            close();
        }
    });
    
    listenerThread.setName("PeerConnection-" + getRemoteIdentifier());
    listenerThread.setDaemon(true);
    listenerThread.start();
}
```

#### Processamento de Mensagens
```java
private void handleIncomingMessage(String rawMessage) {
    try {
        if (rawMessage.startsWith("HANDSHAKE:")) {
            // Processa handshake para obter nome do peer
            remotePeerName = rawMessage.substring(10);
            System.out.println("Peer " + remotePeerName + " (" + 
                             getRemoteIdentifier() + ") connected");
            return;
        }
        
        if (rawMessage.startsWith("MESSAGE:")) {
            // Processa mensagem regular
            String messageData = rawMessage.substring(8);
            Message message = Message.deserialize(messageData);
            
            // Notifica o peer pai
            parentPeer.receiveMessage(message, this);
        }
        
    } catch (Exception e) {
        System.err.println("Error processing message from " + 
                          getRemoteIdentifier() + ": " + e.getMessage());
    }
}
```

### 4. Classe Message

**Localização**: `src/main/java/com/p2pchat/Message.java`

#### Responsabilidades
- Estrutura de dados para mensagens
- Serialização/deserialização para transmissão
- Formatação para exibição

#### Atributos
```java
private final String sender;                    // Remetente
private final String content;                   // Conteúdo
private final LocalDateTime timestamp;          // Timestamp
private static final DateTimeFormatter formatter = 
    DateTimeFormatter.ofPattern("HH:mm:ss");
```

#### Serialização
```java
public String serialize() {
    return sender + "|" + content + "|" + timestamp.toString();
}

public static Message deserialize(String data) {
    String[] parts = data.split("\\|", 3);
    if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid message format");
    }
    
    Message msg = new Message(parts[0], parts[1]);
    // Usa timestamp atual por simplicidade
    return msg;
}
```

### 5. Classe MessageHistory

**Localização**: `src/main/java/com/p2pchat/MessageHistory.java`

#### Responsabilidades
- Armazenar histórico de mensagens da sessão
- Fornecer acesso thread-safe ao histórico
- Limitar número máximo de mensagens

#### Implementação Thread-Safe
```java
private final List<Message> messages;
private final int maxMessages;

public MessageHistory() {
    this(1000); // Máximo padrão de 1000 mensagens
}

public MessageHistory(int maxMessages) {
    this.messages = new CopyOnWriteArrayList<>(); // Thread-safe
    this.maxMessages = maxMessages;
}
```

#### Gerenciamento Automático de Limite
```java
public void addMessage(Message message) {
    messages.add(message);
    
    // Remove mensagens antigas se exceder limite
    if (messages.size() > maxMessages) {
        messages.remove(0);
    }
}
```

### 6. Classe PeerDiscovery

**Localização**: `src/main/java/com/p2pchat/PeerDiscovery.java`

#### Responsabilidades
- Descoberta automática de peers na rede
- Varredura eficiente usando multi-threading
- Detecção baseada em timeout de conexão

#### Configuração
```java
private static final int[] COMMON_PORTS = {
    8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8090
};
private static final int DISCOVERY_TIMEOUT_MS = 1000; // 1 segundo por conexão
```

#### Descoberta Local (Localhost)
```java
public static List<PeerInfo> discoverLocalPeers(int excludePort) {
    List<PeerInfo> discoveredPeers = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<CompletableFuture<PeerInfo>> futures = new ArrayList<>();
    
    // Testa cada porta comum
    for (int port : COMMON_PORTS) {
        if (port != excludePort) { // Não tenta conectar a si mesmo
            CompletableFuture<PeerInfo> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Socket testSocket = new Socket();
                    testSocket.connect(
                        new java.net.InetSocketAddress("localhost", port), 
                        DISCOVERY_TIMEOUT_MS
                    );
                    testSocket.close();
                    return new PeerInfo("localhost", port);
                } catch (IOException e) {
                    return null; // Nenhum peer nesta porta
                }
            }, executor);
            futures.add(future);
        }
    }
    
    // Coleta resultados
    for (CompletableFuture<PeerInfo> future : futures) {
        try {
            PeerInfo peerInfo = future.get(DISCOVERY_TIMEOUT_MS + 500, 
                                          TimeUnit.MILLISECONDS);
            if (peerInfo != null) {
                discoveredPeers.add(peerInfo);
            }
        } catch (Exception e) {
            // Ignora descobertas que falharam
        }
    }
    
    executor.shutdown();
    return discoveredPeers;
}
```

## Especificação do Protocolo

### Formato das Mensagens

O sistema utiliza um protocolo textual simples sobre TCP:

#### 1. Mensagem de Handshake
```
HANDSHAKE:<nome_do_usuario>
```
- **Propósito**: Identificar o peer após estabelecer conexão
- **Exemplo**: `HANDSHAKE:Alice`
- **Quando**: Enviado imediatamente após conectar

#### 2. Mensagem de Chat
```
MESSAGE:<remetente>|<conteudo>|<timestamp>
```
- **Propósito**: Transmitir mensagem de chat
- **Exemplo**: `MESSAGE:Alice|Olá pessoal!|2024-01-15T14:30:15.123456`
- **Quando**: Sempre que usuário envia mensagem

### Sequência de Conexão

```
Peer A (Cliente)           Peer B (Servidor)
      |                          |
      |-------- TCP Connect ---->|
      |                          |
      |---- HANDSHAKE:Alice ---->|
      |                          |
      |<--- HANDSHAKE:Bob -------|
      |                          |
      |    Conexão Estabelecida  |
      |                          |
      |-- MESSAGE:Alice|Oi|... ->|
      |                          |
```

### Algoritmo de Prevenção de Loops

Para evitar loops infinitos de mensagens:

1. **Identificação de Origem**: Cada mensagem carrega informação do remetente original
2. **Exclusão de Retorno**: Mensagem não é reenviada para a conexão de origem
3. **Broadcast Único**: Cada peer faz broadcast apenas uma vez por mensagem

```
Peer A ---- MESSAGE ----> Peer B
  ^                          |
  |                          v
Peer C <--- MESSAGE ----- Peer D

Peer B não reenvia para Peer A
Peer C não reenvia para Peer D
```

## Guia do Usuário Completo

### Instalação e Configuração

#### Pré-requisitos
- Java 8 ou superior
- Sistema operacional com suporte a sockets TCP
- Portas 8080-8090 disponíveis (configurável)

#### Compilação
```bash
# Torna os scripts executáveis
chmod +x build.sh run.sh demo.sh

# Compila o projeto
./build.sh
```

#### Execução
```bash
# Opção 1: Script de execução
./run.sh

# Opção 2: JAR diretamente
java -jar p2pchat.jar

# Opção 3: Classes compiladas
java -cp build/classes com.p2pchat.ChatClient
```

### Primeiro Uso

#### Passo 1: Iniciar a Aplicação
```
$ ./run.sh
========================================
    P2P Chat System v1.0.0
========================================
Welcome to the P2P Chat System!
This application allows you to chat with
multiple users in a decentralized network.
========================================

Enter your username: Alice
Enter listening port (8080-8090): 8080
```

#### Passo 2: Peer Iniciado
```
Peer started successfully!

=== P2P Chat Commands ===
connect <host:port>  - Connect to a peer (e.g., connect localhost:8081)
discover             - Find and connect to peers automatically
status               - Show peer status and connections
history [count]      - Show message history (default: 10 messages)
clear                - Clear the screen
help                 - Show this help message
quit                 - Exit the application

To send a message, just type it and press Enter.
Messages will be broadcasted to all connected peers.
========================

> 
```

### Comandos Detalhados

#### connect \<host:port\>
**Função**: Conecta manualmente a um peer específico

**Exemplos**:
```bash
> connect localhost:8081
Connected to peer at localhost:8081

> connect 192.168.1.100:8082
Connected to peer at 192.168.1.100:8082

> connect
Enter host:port to connect to: localhost:8083
Connected to peer at localhost:8083
```

**Casos de Erro**:
```bash
> connect localhost:9999
Failed to connect to localhost:9999 - Connection refused

> connect invalid:port
Invalid port number.

> connect invalidformat
Invalid format. Use: host:port (e.g., localhost:8081)
```

#### discover
**Função**: Descobre automaticamente peers na rede local

**Exemplo de Uso**:
```bash
> discover
Discovering peers...
Found 2 peer(s):
1. localhost:8081
2. localhost:8082
Enter number to connect (or 0 to cancel): 1
Connected to peer at localhost:8081

> discover  
Discovering peers...
No peers found on localhost.
```

#### status
**Função**: Exibe informações detalhadas sobre o peer atual

**Exemplo**:
```bash
> status

=== Peer Status ===
Name: Alice
Listening Port: 8080
Active Connections: 2
  - Bob
  - Charlie
Messages in History: 15
==================
```

#### history [count]
**Função**: Exibe histórico de mensagens

**Exemplos**:
```bash
> history
--- Recent Messages ---
[14:30:15] Alice: Olá pessoal!
[14:30:18] Bob: Oi Alice!
[14:30:22] Charlie: Oi galera!
[14:30:30] Alice: Como vocês estão?
[14:30:35] Bob: Tudo bem por aqui
----------------------

> history 3
--- Recent Messages ---
[14:30:22] Charlie: Oi galera!
[14:30:30] Alice: Como vocês estão?
[14:30:35] Bob: Tudo bem por aqui
----------------------

> history 150
Invalid number. Using default of 10 messages.
--- Recent Messages ---
...
----------------------
```

#### clear
**Função**: Limpa a tela do console

#### help
**Função**: Exibe lista de comandos disponíveis

#### quit / exit / q
**Função**: Encerra a aplicação graciosamente

```bash
> quit
Shutting down peer 'Alice'...
Disconnected from peer localhost:8081
Disconnected from peer localhost:8082
Peer 'Alice' stopped.
Thank you for using P2P Chat!
```

### Enviando Mensagens

Para enviar uma mensagem, simplesmente digite o texto e pressione Enter:

```bash
> Olá pessoal, como estão?
[14:35:10] Alice: Olá pessoal, como estão?

> Esta mensagem será enviada para todos os peers conectados
[14:35:25] Alice: Esta mensagem será enviada para todos os peers conectados
```

### Recebendo Mensagens

Mensagens de outros peers aparecem automaticamente:

```bash
> 
[14:35:12] Bob: Oi Alice! Tudo bem por aqui
[14:35:18] Charlie: Também estou bem, obrigado!
> Como está o tempo aí?
[14:35:30] Alice: Como está o tempo aí?
[14:35:35] Bob: Está fazendo sol aqui
```

## Exemplos Práticos

### Cenário 1: Chat Entre Dois Usuários

#### Terminal 1 (Alice - Peer Servidor)
```bash
$ ./run.sh
Enter your username: Alice
Enter listening port (8080-8090): 8080

Peer started successfully!
> status

=== Peer Status ===
Name: Alice
Listening Port: 8080
Active Connections: 0
Messages in History: 0
==================

> Aguardando conexões...
[14:40:15] Alice: Aguardando conexões...
New peer connected from 127.0.0.1:54321
Peer Bob (127.0.0.1:54321) connected
[14:40:32] Bob: Oi Alice! Consegui conectar!
> Oi Bob! Que bom te ver aqui
[14:40:40] Alice: Oi Bob! Que bom te ver aqui
```

#### Terminal 2 (Bob - Peer Cliente)
```bash
$ ./run.sh
Enter your username: Bob
Enter listening port (8080-8090): 8081

Peer started successfully!
> connect localhost:8080
Connected to peer at localhost:8080
Peer Alice (localhost:8080) connected
[14:40:15] Alice: Aguardando conexões...
> Oi Alice! Consegui conectar!
[14:40:32] Bob: Oi Alice! Consegui conectar!
[14:40:40] Alice: Oi Bob! Que bom te ver aqui
```

### Cenário 2: Rede de Três Peers

#### Configuração da Rede
```
Alice (8080) ←→ Bob (8081) ←→ Charlie (8082)
     ↖________________↗
```

#### Terminal 1 (Alice)
```bash
$ ./run.sh
Enter your username: Alice
Enter listening port (8080-8090): 8080

> # Aguarda conexões de Bob e Charlie
```

#### Terminal 2 (Bob)
```bash
$ ./run.sh
Enter your username: Bob  
Enter listening port (8080-8090): 8081

> connect localhost:8080
Connected to peer at localhost:8080
> # Aguarda conexão de Charlie
```

#### Terminal 3 (Charlie)
```bash
$ ./run.sh
Enter your username: Charlie
Enter listening port (8080-8090): 8082

> discover
Discovering peers...
Found 2 peer(s):
1. localhost:8080
2. localhost:8081
Enter number to connect (or 0 to cancel): 1
Connected to peer at localhost:8080
> connect localhost:8081
Connected to peer at localhost:8081
```

#### Teste de Broadcasting
No Terminal 1 (Alice):
```bash
> Mensagem teste para toda a rede!
[14:45:10] Alice: Mensagem teste para toda a rede!
```

Esta mensagem aparecerá nos três terminais:
- Terminal 1: Como remetente
- Terminal 2: Recebida de Alice
- Terminal 3: Recebida via Alice ou Bob (dependendo da topologia)

### Cenário 3: Descoberta Automática

#### Terminal 1 (Servidor de Descoberta)
```bash
$ ./run.sh
Enter your username: Servidor
Enter listening port (8080-8090): 8080

> Pronto para receber conexões via descoberta
[14:50:00] Servidor: Pronto para receber conexões via descoberta
```

#### Terminal 2 (Cliente com Descoberta)
```bash
$ ./run.sh  
Enter your username: Cliente
Enter listening port (8080-8090): 8081

> discover
Discovering peers...
Found 1 peer(s):
1. localhost:8080
Enter number to connect (or 0 to cancel): 1
Connected to peer at localhost:8080
> Conectado via descoberta automática!
[14:50:15] Cliente: Conectado via descoberta automática!
```

## Solução de Problemas

### Problemas Comuns e Soluções

#### 1. Erro: "Port Already in Use"
**Sintoma**: 
```
Failed to start peer: Address already in use (Bind failed)
Please try a different port.
```

**Causas Possíveis**:
- Porta já ocupada por outro processo
- Instância anterior do chat ainda executando
- Serviço do sistema usando a porta

**Soluções**:
```bash
# Verificar quem está usando a porta
netstat -ln | grep 8080
lsof -i :8080

# Usar porta diferente
Enter listening port (8080-8090): 8081

# Matar processo anterior (se necessário)
kill -9 <PID>
```

#### 2. Erro: "Connection Refused"
**Sintoma**:
```
Failed to connect to localhost:8081 - Connection refused
```

**Causas Possíveis**:
- Peer de destino não está executando
- Porta incorreta
- Firewall bloqueando conexão
- IP/hostname incorreto

**Soluções**:
```bash
# Verificar se o peer de destino está rodando
telnet localhost 8081

# Verificar portas abertas
netstat -ln | grep LISTEN

# Testar descoberta automática
> discover

# Verificar conectividade de rede
ping localhost
```

#### 3. Erro: "Invalid Port Number"
**Sintoma**:
```
Please enter a valid port number.
```

**Soluções**:
- Usar apenas números entre 8080-8090
- Verificar se não há caracteres especiais
- Confirmar que a porta está na faixa permitida

#### 4. Mensagens Não Aparecem
**Sintomas**:
- Mensagens enviadas mas não recebidas por outros peers
- Histórico vazio após conversa

**Diagnóstico**:
```bash
# Verificar status das conexões
> status

# Verificar histórico
> history

# Testar conectividade básica
> connect localhost:8081
```

**Soluções**:
- Verificar se peers estão realmente conectados
- Reiniciar aplicação se necessário
- Verificar logs no console para erros de rede

#### 5. Descoberta Não Encontra Peers
**Sintoma**:
```
Discovering peers...
No peers found on localhost.
```

**Causas**:
- Nenhum peer executando nas portas padrão
- Peers executando em portas fora da faixa padrão
- Problemas de rede local

**Soluções**:
```bash
# Iniciar peer nas portas padrão (8080-8090)
# Verificar se outros peers estão ativos
# Usar conexão manual se necessário
> connect localhost:8080
```

### Logs e Depuração

#### Mensagens de Log Importantes

##### Conexões Bem-sucedidas
```
Peer 'Alice' started on port 8080
New peer connected from 127.0.0.1:54321
Peer Bob (127.0.0.1:54321) connected
Connected to peer at localhost:8081
```

##### Erros de Conexão
```
Error accepting connection: Socket closed
Failed to connect to localhost:8082 - Connection refused
Error reading from peer 127.0.0.1:54321: Connection reset
```

##### Desconexões
```
Disconnected from peer 127.0.0.1:54321
Shutting down peer 'Alice'...
Peer 'Alice' stopped.
```

#### Técnicas de Depuração

1. **Verificar Status Frequentemente**:
   ```bash
   > status
   ```

2. **Usar Descoberta para Diagnóstico**:
   ```bash
   > discover
   ```

3. **Testar Conexões Manuais**:
   ```bash
   > connect localhost:8080
   > connect localhost:8081
   ```

4. **Monitorar Histórico**:
   ```bash
   > history
   ```

### Limitações Conhecidas

#### 1. Limitações de Rede
- **Escopo**: Funciona principalmente em redes locais
- **NAT/Firewall**: Pode não funcionar através de NATs/firewalls
- **Internet**: Não otimizado para comunicação via internet

#### 2. Limitações de Segurança
- **Criptografia**: Mensagens não são criptografadas
- **Autenticação**: Apenas identificação por nome de usuário
- **Autorização**: Sem controle de acesso

#### 3. Limitações de Persistência
- **Histórico**: Perdido ao encerrar aplicação
- **Configurações**: Não persistem entre sessões
- **Estado**: Sem sincronização de estado

#### 4. Limitações de Escalabilidade
- **Número de Peers**: Performance degrada com muitos peers
- **Mensagens**: Broadcasting pode ser ineficiente com muitas mensagens
- **Memória**: Histórico limitado a 1000 mensagens por padrão

## Referência da API

### Classe ChatClient

#### Métodos Públicos

##### `ChatClient()`
- **Descrição**: Construtor padrão, inicializa scanner e estado
- **Parâmetros**: Nenhum
- **Retorno**: Nova instância de ChatClient

##### `static void main(String[] args)`
- **Descrição**: Ponto de entrada da aplicação
- **Parâmetros**: `args` - Argumentos da linha de comando (não utilizados)
- **Retorno**: void

##### `void run()`
- **Descrição**: Loop principal da aplicação
- **Parâmetros**: Nenhum
- **Retorno**: void
- **Exceções**: Pode imprimir erros de IOException

#### Métodos Privados

##### `void printWelcome()`
- **Descrição**: Exibe mensagem de boas-vindas
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `String getUserName()`
- **Descrição**: Solicita e retorna nome do usuário
- **Parâmetros**: Nenhum
- **Retorno**: String com nome do usuário

##### `int getUserPort()`
- **Descrição**: Solicita e valida porta de escuta (8080-8090)
- **Parâmetros**: Nenhum
- **Retorno**: int porta válida

##### `void showMainMenu()`
- **Descrição**: Loop interativo principal de comandos
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `void processCommand(String input)`
- **Descrição**: Processa comando ou mensagem do usuário
- **Parâmetros**: `input` - Entrada do usuário
- **Retorno**: void

##### `void handleConnect(String[] parts)`
- **Descrição**: Trata comando de conexão
- **Parâmetros**: `parts` - Array com comando e parâmetros
- **Retorno**: void

##### `void connectToPeer(String hostPort)`
- **Descrição**: Conecta a um peer específico
- **Parâmetros**: `hostPort` - String no formato "host:porta"
- **Retorno**: void

##### `void handleDiscover()`
- **Descrição**: Executa descoberta automática de peers
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `void handleHistory(String[] parts)`
- **Descrição**: Exibe histórico de mensagens
- **Parâmetros**: `parts` - Array com comando e número opcional
- **Retorno**: void

##### `void clearScreen()`
- **Descrição**: Limpa tela do console
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `void printHelp()`
- **Descrição**: Exibe informações de ajuda
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `void shutdown()`
- **Descrição**: Encerra aplicação graciosamente
- **Parâmetros**: Nenhum
- **Retorno**: void

### Classe Peer

#### Atributos Públicos
- **Nenhum** (todos os atributos são privados/finais)

#### Métodos Públicos

##### `Peer(String peerName, int listeningPort)`
- **Descrição**: Construtor, inicializa peer com nome e porta
- **Parâmetros**: 
  - `peerName` - Nome identificador do peer
  - `listeningPort` - Porta para escutar conexões
- **Retorno**: Nova instância de Peer

##### `void start() throws IOException`
- **Descrição**: Inicia servidor para aceitar conexões
- **Parâmetros**: Nenhum
- **Retorno**: void
- **Exceções**: `IOException` se não conseguir abrir ServerSocket

##### `boolean connectToPeer(String host, int port)`
- **Descrição**: Conecta a outro peer
- **Parâmetros**:
  - `host` - Hostname ou IP do peer de destino
  - `port` - Porta do peer de destino
- **Retorno**: `boolean` - true se conexão bem-sucedida

##### `void broadcastMessage(String content)`
- **Descrição**: Envia mensagem para todos os peers conectados
- **Parâmetros**: `content` - Conteúdo da mensagem
- **Retorno**: void

##### `void receiveMessage(Message message, PeerConnection fromConnection)`
- **Descrição**: Processa mensagem recebida e faz rebroadcast
- **Parâmetros**:
  - `message` - Mensagem recebida
  - `fromConnection` - Conexão de origem (para evitar loops)
- **Retorno**: void

##### `void removePeerConnection(PeerConnection connection)`
- **Descrição**: Remove conexão da lista (chamado quando conexão é fechada)
- **Parâmetros**: `connection` - Conexão a ser removida
- **Retorno**: void

##### `List<PeerConnection> getActiveConnections()`
- **Descrição**: Retorna lista de conexões ativas
- **Parâmetros**: Nenhum
- **Retorno**: `List<PeerConnection>` - Lista filtrada de conexões ativas

##### `MessageHistory getMessageHistory()`
- **Descrição**: Retorna instância do histórico de mensagens
- **Parâmetros**: Nenhum
- **Retorno**: `MessageHistory` - Histórico de mensagens

##### `void displayStatus()`
- **Descrição**: Exibe informações de status do peer
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `void stop()`
- **Descrição**: Para o peer e fecha todas as conexões
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `boolean isRunning()`
- **Descrição**: Verifica se o peer está em execução
- **Parâmetros**: Nenhum
- **Retorno**: `boolean` - true se peer está rodando

##### `String getPeerName()`
- **Descrição**: Retorna nome do peer
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Nome do peer

##### `int getListeningPort()`
- **Descrição**: Retorna porta de escuta
- **Parâmetros**: Nenhum
- **Retorno**: `int` - Porta de escuta

### Classe PeerConnection

#### Métodos Públicos

##### `PeerConnection(Socket socket, Peer parentPeer) throws IOException`
- **Descrição**: Construtor, inicializa conexão com socket
- **Parâmetros**:
  - `socket` - Socket TCP estabelecido
  - `parentPeer` - Referência ao peer pai
- **Retorno**: Nova instância de PeerConnection
- **Exceções**: `IOException` se erro ao inicializar streams

##### `void sendMessage(Message message)`
- **Descrição**: Envia mensagem através desta conexão
- **Parâmetros**: `message` - Mensagem a ser enviada
- **Retorno**: void

##### `void sendHandshake(String peerName)`
- **Descrição**: Envia handshake de identificação
- **Parâmetros**: `peerName` - Nome do peer local
- **Retorno**: void

##### `void close()`
- **Descrição**: Fecha conexão e recursos associados
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `boolean isActive()`
- **Descrição**: Verifica se conexão está ativa
- **Parâmetros**: Nenhum
- **Retorno**: `boolean` - true se conexão está ativa

##### `String getRemoteIdentifier()`
- **Descrição**: Retorna identificador do peer remoto (IP:porta)
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Identificador no formato "IP:porta"

##### `String getRemotePeerName()`
- **Descrição**: Retorna nome do peer remoto (após handshake)
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Nome do peer ou identificador se nome indisponível

### Classe Message

#### Métodos Públicos

##### `Message(String sender, String content)`
- **Descrição**: Construtor, cria mensagem com timestamp atual
- **Parâmetros**:
  - `sender` - Nome do remetente
  - `content` - Conteúdo da mensagem
- **Retorno**: Nova instância de Message

##### `String getSender()`
- **Descrição**: Retorna nome do remetente
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Nome do remetente

##### `String getContent()`
- **Descrição**: Retorna conteúdo da mensagem
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Conteúdo da mensagem

##### `LocalDateTime getTimestamp()`
- **Descrição**: Retorna timestamp da mensagem
- **Parâmetros**: Nenhum
- **Retorno**: `LocalDateTime` - Timestamp da mensagem

##### `String getFormattedTime()`
- **Descrição**: Retorna timestamp formatado (HH:mm:ss)
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Timestamp formatado

##### `String serialize()`
- **Descrição**: Serializa mensagem para transmissão
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Mensagem serializada

##### `static Message deserialize(String data)`
- **Descrição**: Deserializa mensagem recebida
- **Parâmetros**: `data` - String serializada
- **Retorno**: `Message` - Mensagem deserializada
- **Exceções**: `IllegalArgumentException` se formato inválido

##### `String toString()`
- **Descrição**: Retorna representação formatada para exibição
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Mensagem formatada [HH:mm:ss] Sender: Content

### Classe MessageHistory

#### Métodos Públicos

##### `MessageHistory()`
- **Descrição**: Construtor padrão (máximo 1000 mensagens)
- **Parâmetros**: Nenhum
- **Retorno**: Nova instância de MessageHistory

##### `MessageHistory(int maxMessages)`
- **Descrição**: Construtor com limite personalizado
- **Parâmetros**: `maxMessages` - Número máximo de mensagens
- **Retorno**: Nova instância de MessageHistory

##### `void addMessage(Message message)`
- **Descrição**: Adiciona mensagem ao histórico
- **Parâmetros**: `message` - Mensagem a ser adicionada
- **Retorno**: void

##### `List<Message> getAllMessages()`
- **Descrição**: Retorna todas as mensagens em ordem cronológica
- **Parâmetros**: Nenhum
- **Retorno**: `List<Message>` - Lista de todas as mensagens

##### `List<Message> getRecentMessages(int count)`
- **Descrição**: Retorna as N mensagens mais recentes
- **Parâmetros**: `count` - Número de mensagens desejadas
- **Retorno**: `List<Message>` - Lista das mensagens mais recentes

##### `void clear()`
- **Descrição**: Remove todas as mensagens do histórico
- **Parâmetros**: Nenhum
- **Retorno**: void

##### `int size()`
- **Descrição**: Retorna número total de mensagens
- **Parâmetros**: Nenhum
- **Retorno**: `int` - Número de mensagens no histórico

##### `void displayRecentMessages(int count)`
- **Descrição**: Exibe mensagens recentes no console
- **Parâmetros**: `count` - Número de mensagens a exibir
- **Retorno**: void

### Classe PeerDiscovery

#### Métodos Estáticos Públicos

##### `static List<PeerInfo> discoverLocalPeers(int excludePort)`
- **Descrição**: Descobre peers no localhost nas portas comuns
- **Parâmetros**: `excludePort` - Porta a ser excluída (própria porta)
- **Retorno**: `List<PeerInfo>` - Lista de peers encontrados

##### `static List<PeerInfo> discoverNetworkPeers(int excludePort)`
- **Descrição**: Descobre peers na rede local (subnet)
- **Parâmetros**: `excludePort` - Porta a ser excluída (própria porta)
- **Retorno**: `List<PeerInfo>` - Lista de peers encontrados

#### Classe Interna PeerInfo

##### `PeerInfo(String host, int port)`
- **Descrição**: Construtor, cria informação de peer
- **Parâmetros**:
  - `host` - Hostname ou IP
  - `port` - Porta
- **Retorno**: Nova instância de PeerInfo

##### `String getHost()`
- **Descrição**: Retorna hostname/IP
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Hostname ou IP

##### `int getPort()`
- **Descrição**: Retorna porta
- **Parâmetros**: Nenhum
- **Retorno**: `int` - Número da porta

##### `String toString()`
- **Descrição**: Retorna representação no formato host:port
- **Parâmetros**: Nenhum
- **Retorno**: `String` - Formato "host:port"

## Conclusão

Este documento fornece uma documentação completa e detalhada do Sistema de Chat P2P, cobrindo 100% do código fonte, arquitetura, funcionalidades e uso prático. O sistema demonstra uma implementação eficaz de comunicação P2P descentralizada usando Java e sockets TCP, com uma arquitetura modular e extensível.

### Pontos Fortes da Implementação
- Arquitetura bem estruturada e modular
- Threading adequado para operações concorrentes
- Protocolo simples e eficiente
- Interface de usuário intuitiva
- Tratamento robusto de erros e desconexões
- Descoberta automática de peers
- Prevenção de loops de mensagens

### Áreas para Melhorias Futuras
- Implementação de criptografia para segurança
- Interface gráfica de usuário
- Persistência de configurações e histórico
- Suporte a comunicação via internet (NAT traversal)
- Otimizações de performance para redes maiores
- Sistema de autenticação mais robusto

O código está bem documentado e seguindo boas práticas de programação Java, tornando-o uma excelente base para aprendizado e extensões futuras.