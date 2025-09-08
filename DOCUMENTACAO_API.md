# Documentação da API - Sistema de Chat P2P

## Visão Geral da API

Esta documentação fornece detalhes técnicos completos sobre todas as classes, métodos, atributos e interfaces do Sistema de Chat P2P. É destinada a desenvolvedores que desejam entender, modificar ou estender o código.

## Estrutura dos Pacotes

```
com.p2pchat
├── ChatClient.java          # Interface principal do usuário
├── Peer.java               # Núcleo do sistema P2P
├── PeerConnection.java     # Gerenciamento de conexões
├── Message.java            # Estrutura de dados de mensagens
├── MessageHistory.java     # Histórico de mensagens
└── PeerDiscovery.java      # Descoberta automática de peers
```

## Documentação Detalhada das Classes

### ChatClient

```java
package com.p2pchat;

/**
 * Classe principal que fornece a interface de usuário para o sistema de chat P2P.
 * 
 * Esta classe gerencia a interação com o usuário através de uma interface de console,
 * processando comandos e coordenando a comunicação com o núcleo P2P.
 * 
 * @author Sistema P2P Chat
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChatClient {
    
    /**
     * Versão atual da aplicação.
     * Usado para identificação e compatibilidade.
     */
    private static final String VERSION = "1.0.0";
    
    /**
     * Instância do peer P2P.
     * Representa o núcleo funcional do sistema de comunicação.
     */
    private Peer peer;
    
    /**
     * Scanner para entrada do usuário.
     * Usado para ler comandos e mensagens do console.
     */
    private Scanner scanner;
    
    /**
     * Flag de controle da execução da aplicação.
     * Quando false, a aplicação deve ser encerrada.
     */
    private boolean running;
    
    /**
     * Construtor padrão da classe ChatClient.
     * 
     * Inicializa o scanner para entrada do usuário e define o estado
     * inicial como "executando".
     */
    public ChatClient() {
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    /**
     * Ponto de entrada principal da aplicação.
     * 
     * Cria uma nova instância do ChatClient e inicia sua execução.
     * Este método é chamado pela JVM quando a aplicação é iniciada.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.run();
    }
    
    /**
     * Loop principal de execução da aplicação.
     * 
     * Este método coordena toda a execução da aplicação:
     * 1. Exibe mensagem de boas-vindas
     * 2. Coleta informações do usuário (nome e porta)
     * 3. Inicia o peer P2P
     * 4. Configura hook de shutdown gracioso
     * 5. Entra no loop de comandos interativo
     * 
     * @throws RuntimeException se houver erro na inicialização do peer
     */
    public void run() {
        printWelcome();
        
        // Get user information
        String userName = getUserName();
        int port = getUserPort();
        
        try {
            // Create and start the peer
            peer = new Peer(userName, port);
            peer.start();
            
            // Set up shutdown hook for graceful exit
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            
            // Show main menu
            showMainMenu();
            
        } catch (IOException e) {
            System.err.println("Failed to start peer: " + e.getMessage());
            System.err.println("Please try a different port.");
        }
        
        shutdown();
    }
    
    /**
     * Exibe a mensagem de boas-vindas da aplicação.
     * 
     * Mostra o banner do sistema com versão e informações básicas
     * sobre o propósito da aplicação.
     */
    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("    P2P Chat System v" + VERSION);
        System.out.println("========================================");
        System.out.println("Welcome to the P2P Chat System!");
        System.out.println("This application allows you to chat with");
        System.out.println("multiple users in a decentralized network.");
        System.out.println("========================================\n");
    }
    
    /**
     * Solicita e obtém o nome do usuário.
     * 
     * @return String contendo o nome fornecido pelo usuário
     */
    private String getUserName() {
        System.out.print("Enter your username: ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Solicita e valida a porta de escuta do usuário.
     * 
     * Continua solicitando até que uma porta válida (8080-8090) seja fornecida.
     * Trata automaticamente entradas inválidas e fornece feedback ao usuário.
     * 
     * @return int porta válida entre 8080 e 8090
     */
    private int getUserPort() {
        while (true) {
            System.out.print("Enter listening port (8080-8090): ");
            try {
                int port = Integer.parseInt(scanner.nextLine().trim());
                if (port >= 8080 && port <= 8090) {
                    return port;
                } else {
                    System.out.println("Please enter a port between 8080 and 8090.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid port number.");
            }
        }
    }
    
    /**
     * Loop principal de comandos interativos.
     * 
     * Exibe o menu de ajuda inicial e então entra em um loop de processamento
     * de comandos que continua até que o usuário escolha sair ou o peer pare.
     */
    private void showMainMenu() {
        System.out.println("\nPeer started successfully!");
        printHelp();
        
        while (running && peer.isRunning()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            processCommand(input);
        }
    }
    
    /**
     * Processa comandos do usuário ou mensagens de chat.
     * 
     * Analisa a entrada do usuário e executa a ação apropriada:
     * - Comandos especiais (help, connect, discover, etc.)
     * - Mensagens de chat (qualquer texto que não seja comando)
     * 
     * @param input String contendo o comando ou mensagem do usuário
     */
    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help":
            case "h":
                printHelp();
                break;
                
            case "connect":
            case "c":
                handleConnect(parts);
                break;
                
            case "discover":
            case "d":
                handleDiscover();
                break;
                
            case "status":
            case "s":
                peer.displayStatus();
                break;
                
            case "history":
            case "hist":
                handleHistory(parts);
                break;
                
            case "clear":
                clearScreen();
                break;
                
            case "quit":
            case "exit":
            case "q":
                running = false;
                break;
                
            default:
                // Treat as a chat message
                if (!input.startsWith("/")) {
                    peer.broadcastMessage(input);
                } else {
                    System.out.println("Unknown command. Type 'help' for available commands.");
                }
                break;
        }
    }
    
    /**
     * Manipula o comando de conexão a um peer específico.
     * 
     * Se nenhum parâmetro for fornecido, solicita entrada interativa.
     * Valida formato e tenta estabelecer conexão.
     * 
     * @param parts Array contendo o comando e parâmetros opcionais
     */
    private void handleConnect(String[] parts) {
        if (parts.length < 2) {
            System.out.print("Enter host:port to connect to: ");
            String hostPort = scanner.nextLine().trim();
            if (!hostPort.isEmpty()) {
                connectToPeer(hostPort);
            }
        } else {
            connectToPeer(parts[1]);
        }
    }
    
    /**
     * Conecta a um peer específico dado uma string host:porta.
     * 
     * Valida o formato da entrada e tenta estabelecer conexão TCP.
     * Fornece feedback apropriado sobre sucesso ou falha.
     * 
     * @param hostPort String no formato "host:porta" (ex: "localhost:8081")
     */
    private void connectToPeer(String hostPort) {
        try {
            String[] parts = hostPort.split(":", 2);
            if (parts.length != 2) {
                System.out.println("Invalid format. Use: host:port (e.g., localhost:8081)");
                return;
            }
            
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            peer.connectToPeer(host, port);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
        }
    }
    
    /**
     * Executa descoberta automática de peers na rede local.
     * 
     * Usa o PeerDiscovery para encontrar peers nas portas comuns do localhost.
     * Se peers forem encontrados, permite ao usuário selecionar um para conectar.
     */
    private void handleDiscover() {
        System.out.println("Discovering peers...");
        
        List<PeerDiscovery.PeerInfo> localPeers = PeerDiscovery.discoverLocalPeers(peer.getListeningPort());
        
        if (localPeers.isEmpty()) {
            System.out.println("No peers found on localhost.");
        } else {
            System.out.println("Found " + localPeers.size() + " peer(s):");
            for (int i = 0; i < localPeers.size(); i++) {
                PeerDiscovery.PeerInfo peerInfo = localPeers.get(i);
                System.out.println((i + 1) + ". " + peerInfo);
            }
            
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
    
    /**
     * Manipula o comando de exibição do histórico de mensagens.
     * 
     * Permite especificar o número de mensagens a exibir (1-100).
     * Se não especificado, usa o padrão de 10 mensagens.
     * 
     * @param parts Array contendo comando e número opcional de mensagens
     */
    private void handleHistory(String[] parts) {
        int count = 10; // Default to last 10 messages
        
        if (parts.length > 1) {
            try {
                count = Integer.parseInt(parts[1]);
                count = Math.max(1, Math.min(count, 100)); // Limit between 1 and 100
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Using default of 10 messages.");
            }
        }
        
        peer.getMessageHistory().displayRecentMessages(count);
    }
    
    /**
     * Limpa a tela do console usando códigos de escape ANSI.
     * 
     * Funciona na maioria dos terminais modernos.
     */
    private void clearScreen() {
        // Try to clear screen (works on most terminals)
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }
    
    /**
     * Exibe informações de ajuda e lista de comandos disponíveis.
     * 
     * Mostra todos os comandos suportados com suas descrições e exemplos de uso.
     */
    private void printHelp() {
        System.out.println("\n=== P2P Chat Commands ===");
        System.out.println("connect <host:port>  - Connect to a peer (e.g., connect localhost:8081)");
        System.out.println("discover             - Find and connect to peers automatically");
        System.out.println("status               - Show peer status and connections");
        System.out.println("history [count]      - Show message history (default: 10 messages)");
        System.out.println("clear                - Clear the screen");
        System.out.println("help                 - Show this help message");
        System.out.println("quit                 - Exit the application");
        System.out.println();
        System.out.println("To send a message, just type it and press Enter.");
        System.out.println("Messages will be broadcasted to all connected peers.");
        System.out.println("========================\n");
    }
    
    /**
     * Encerra a aplicação de forma graceful.
     * 
     * Para o peer, fecha o scanner e exibe mensagem de despedida.
     * Este método é thread-safe e pode ser chamado múltiplas vezes.
     */
    private void shutdown() {
        running = false;
        
        if (peer != null) {
            peer.stop();
        }
        
        if (scanner != null) {
            scanner.close();
        }
        
        System.out.println("Thank you for using P2P Chat!");
    }
}
```

### Peer

```java
package com.p2pchat;

/**
 * Classe principal do sistema P2P que gerencia múltiplas conexões e broadcasting de mensagens.
 * 
 * Cada peer atua simultaneamente como servidor (aceitando conexões) e cliente (conectando
 * a outros peers). Esta classe coordena toda a funcionalidade de rede e comunicação.
 * 
 * @author Sistema P2P Chat
 * @version 1.0.0
 * @since 1.0.0
 */
public class Peer {
    
    /**
     * Nome identificador do peer.
     * Usado para identificar o peer na rede e nas mensagens.
     */
    private final String peerName;
    
    /**
     * Porta na qual este peer escuta conexões de entrada.
     * Deve estar na faixa 8080-8090 por convenção.
     */
    private final int listeningPort;
    
    /**
     * Lista thread-safe de todas as conexões ativas com outros peers.
     * Usa CopyOnWriteArrayList para permitir acesso concorrente seguro.
     */
    private final List<PeerConnection> connections;
    
    /**
     * Histórico de mensagens da sessão atual.
     * Mantém todas as mensagens enviadas e recebidas durante a execução.
     */
    private final MessageHistory messageHistory;
    
    /**
     * Flag atômico indicando se o peer está em execução.
     * Thread-safe para acesso de múltiplas threads.
     */
    private final AtomicBoolean running;
    
    /**
     * Socket servidor para aceitar conexões de entrada.
     * Null se o peer não estiver executando.
     */
    private ServerSocket serverSocket;
    
    /**
     * Construtor do Peer.
     * 
     * Inicializa todas as estruturas de dados necessárias mas não inicia
     * o servidor. O método start() deve ser chamado explicitamente.
     * 
     * @param peerName Nome identificador do peer
     * @param listeningPort Porta para aceitar conexões (recomendado 8080-8090)
     */
    public Peer(String peerName, int listeningPort) {
        this.peerName = peerName;
        this.listeningPort = listeningPort;
        this.connections = new CopyOnWriteArrayList<>();
        this.messageHistory = new MessageHistory();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Inicia o servidor peer para aceitar conexões de entrada.
     * 
     * Cria um ServerSocket na porta especificada e inicia uma thread daemon
     * para aceitar conexões. O método é idempotente - múltiplas chamadas
     * não têm efeito adicional.
     * 
     * @throws IOException se não conseguir abrir o ServerSocket na porta especificada
     */
    public void start() throws IOException {
        if (running.get()) {
            return;
        }
        
        serverSocket = new ServerSocket(listeningPort);
        running.set(true);
        
        System.out.println("Peer '" + peerName + "' started on port " + listeningPort);
        
        // Start accepting connections in a separate thread
        Thread serverThread = new Thread(this::acceptConnections);
        serverThread.setName("PeerServer-" + peerName);
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Loop de aceitação de conexões que roda em thread separada.
     * 
     * Continuamente aceita novas conexões TCP e cria objetos PeerConnection
     * para gerenciá-las. Cada nova conexão recebe um handshake imediato.
     * 
     * Este método roda até que o peer seja parado ou ocorra erro fatal.
     */
    private void acceptConnections() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Create new peer connection
                PeerConnection connection = new PeerConnection(clientSocket, this);
                connections.add(connection);
                
                // Send handshake
                connection.sendHandshake(peerName);
                
                System.out.println("New peer connected from " + connection.getRemoteIdentifier());
                
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Conecta a outro peer como cliente.
     * 
     * Estabelece conexão TCP com o peer especificado e cria um PeerConnection
     * para gerenciar a comunicação. Envia handshake imediatamente após conectar.
     * 
     * @param host Hostname ou endereço IP do peer de destino
     * @param port Porta do peer de destino
     * @return true se conexão bem-sucedida, false caso contrário
     */
    public boolean connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            
            // Create new peer connection
            PeerConnection connection = new PeerConnection(socket, this);
            connections.add(connection);
            
            // Send handshake
            connection.sendHandshake(peerName);
            
            System.out.println("Connected to peer at " + host + ":" + port);
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to connect to " + host + ":" + port + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envia uma mensagem para todos os peers conectados (broadcast).
     * 
     * Cria um objeto Message com o conteúdo fornecido, adiciona ao histórico
     * local, exibe localmente e então envia para todos os peers ativos.
     * 
     * @param content Conteúdo da mensagem a ser enviada
     */
    public void broadcastMessage(String content) {
        Message message = new Message(peerName, content);
        
        // Add to local message history
        messageHistory.addMessage(message);
        
        // Display locally
        System.out.println(message);
        
        // Send to all connected peers
        for (PeerConnection connection : connections) {
            if (connection.isActive()) {
                connection.sendMessage(message);
            }
        }
    }
    
    /**
     * Processa uma mensagem recebida de outro peer.
     * 
     * Adiciona a mensagem ao histórico local, exibe na tela e faz o reencaminhamento
     * (flooding) para todos os outros peers conectados, exceto o remetente original.
     * Este algoritmo implementa a distribuição de mensagens na rede P2P.
     * 
     * @param message A mensagem recebida
     * @param fromConnection A conexão de origem (para evitar loops)
     */
    public void receiveMessage(Message message, PeerConnection fromConnection) {
        // Add to message history
        messageHistory.addMessage(message);
        
        // Display the message
        System.out.println(message);
        
        // Forward the message to all other peers (excluding the sender)
        for (PeerConnection connection : connections) {
            if (connection != fromConnection && connection.isActive()) {
                connection.sendMessage(message);
            }
        }
    }
    
    /**
     * Remove uma conexão peer da lista de conexões ativas.
     * 
     * Chamado automaticamente quando uma PeerConnection é fechada.
     * Thread-safe devido ao uso de CopyOnWriteArrayList.
     * 
     * @param connection A conexão a ser removida
     */
    public void removePeerConnection(PeerConnection connection) {
        connections.remove(connection);
    }
    
    /**
     * Retorna lista de conexões atualmente ativas.
     * 
     * Filtra a lista completa de conexões retornando apenas aquelas
     * que estão ativas no momento da chamada.
     * 
     * @return Lista imutável de conexões ativas
     */
    public List<PeerConnection> getActiveConnections() {
        return connections.stream()
                .filter(PeerConnection::isActive)
                .toList();
    }
    
    /**
     * Retorna a instância do histórico de mensagens.
     * 
     * Permite acesso ao histórico para visualização ou outras operações.
     * O histórico retornado é thread-safe.
     * 
     * @return Instância do MessageHistory
     */
    public MessageHistory getMessageHistory() {
        return messageHistory;
    }
    
    /**
     * Exibe informações de status atual do peer.
     * 
     * Mostra nome, porta, número de conexões ativas, lista de peers conectados
     * e número total de mensagens no histórico.
     */
    public void displayStatus() {
        System.out.println("\n=== Peer Status ===");
        System.out.println("Name: " + peerName);
        System.out.println("Listening Port: " + listeningPort);
        System.out.println("Active Connections: " + getActiveConnections().size());
        
        for (PeerConnection conn : getActiveConnections()) {
            System.out.println("  - " + conn.getRemotePeerName());
        }
        
        System.out.println("Messages in History: " + messageHistory.size());
        System.out.println("==================\n");
    }
    
    /**
     * Para o peer e fecha todas as conexões de forma graceful.
     * 
     * Este método é thread-safe e idempotente. Para o servidor, fecha todas
     * as conexões peer ativas e limpa recursos. Pode ser chamado múltiplas
     * vezes sem efeitos colaterais.
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        
        System.out.println("Shutting down peer '" + peerName + "'...");
        
        // Close all peer connections
        for (PeerConnection connection : connections) {
            connection.close();
        }
        connections.clear();
        
        // Close server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        System.out.println("Peer '" + peerName + "' stopped.");
    }
    
    /**
     * Verifica se o peer está atualmente em execução.
     * 
     * @return true se o peer está rodando, false caso contrário
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Retorna o nome do peer.
     * 
     * @return String contendo o nome do peer
     */
    public String getPeerName() {
        return peerName;
    }
    
    /**
     * Retorna a porta de escuta do peer.
     * 
     * @return int com o número da porta
     */
    public int getListeningPort() {
        return listeningPort;
    }
}
```

### PeerConnection

```java
package com.p2pchat;

/**
 * Representa uma conexão individual com outro peer na rede P2P.
 * 
 * Gerencia a comunicação bidirecional através de um socket TCP, implementa
 * o protocolo de mensagens e executa em sua própria thread para não bloquear
 * outras operações.
 * 
 * @author Sistema P2P Chat
 * @version 1.0.0
 * @since 1.0.0
 */
public class PeerConnection {
    
    /**
     * Socket TCP para comunicação com o peer remoto.
     */
    private final Socket socket;
    
    /**
     * Reader para receber dados do peer remoto.
     * Usa BufferedReader para eficiência na leitura de linhas.
     */
    private final BufferedReader reader;
    
    /**
     * Writer para enviar dados ao peer remoto.
     * Usa PrintWriter com auto-flush para garantir entrega imediata.
     */
    private final PrintWriter writer;
    
    /**
     * Endereço IP do peer remoto.
     * Extraído do socket para identificação.
     */
    private final String remoteAddress;
    
    /**
     * Porta do peer remoto.
     * Porta de origem da conexão (não necessariamente a porta de escuta).
     */
    private final int remotePort;
    
    /**
     * Flag atômico indicando se a conexão está ativa.
     * Thread-safe para verificação de estado.
     */
    private final AtomicBoolean active;
    
    /**
     * Referência ao peer pai que gerencia esta conexão.
     * Usado para callbacks quando mensagens são recebidas.
     */
    private final Peer parentPeer;
    
    /**
     * Nome do peer remoto obtido via handshake.
     * Null até que o handshake seja completado.
     */
    private String remotePeerName;
    
    /**
     * Construtor da PeerConnection.
     * 
     * Inicializa streams de I/O e inicia imediatamente a thread de escuta.
     * O socket deve estar já estabelecido.
     * 
     * @param socket Socket TCP já conectado
     * @param parentPeer Instância do peer que gerencia esta conexão
     * @throws IOException se houver erro ao criar streams de I/O
     */
    public PeerConnection(Socket socket, Peer parentPeer) throws IOException {
        this.socket = socket;
        this.parentPeer = parentPeer;
        this.remoteAddress = socket.getInetAddress().getHostAddress();
        this.remotePort = socket.getPort();
        this.active = new AtomicBoolean(true);
        
        // Initialize I/O streams
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        
        // Start listening for messages in a separate thread
        startListening();
    }
    
    /**
     * Inicia thread de escuta para mensagens do peer remoto.
     * 
     * Cria e inicia uma thread daemon que continuamente lê linhas do socket
     * e processa mensagens recebidas. A thread termina automaticamente quando
     * a conexão é fechada ou ocorre erro.
     */
    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String line;
                while (active.get() && (line = reader.readLine()) != null) {
                    handleIncomingMessage(line);
                }
            } catch (IOException e) {
                if (active.get()) {
                    System.err.println("Error reading from peer " + getRemoteIdentifier() + ": " + e.getMessage());
                }
            } finally {
                close();
            }
        });
        
        listenerThread.setName("PeerConnection-" + getRemoteIdentifier());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Processa mensagens recebidas do peer remoto.
     * 
     * Interpreta o protocolo de mensagens e toma ação apropriada:
     * - HANDSHAKE: Armazena nome do peer remoto
     * - MESSAGE: Deserializa e encaminha para o peer pai
     * 
     * @param rawMessage Linha raw recebida do socket
     */
    private void handleIncomingMessage(String rawMessage) {
        try {
            if (rawMessage.startsWith("HANDSHAKE:")) {
                // Handle handshake message to get peer name
                remotePeerName = rawMessage.substring(10);
                System.out.println("Peer " + remotePeerName + " (" + getRemoteIdentifier() + ") connected");
                return;
            }
            
            if (rawMessage.startsWith("MESSAGE:")) {
                // Handle regular message
                String messageData = rawMessage.substring(8);
                Message message = Message.deserialize(messageData);
                
                // Add to message history and notify parent peer
                parentPeer.receiveMessage(message, this);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing message from " + getRemoteIdentifier() + ": " + e.getMessage());
        }
    }
    
    /**
     * Envia uma mensagem para o peer remoto.
     * 
     * Serializa a mensagem usando o protocolo MESSAGE e envia através do socket.
     * Se a conexão não estiver ativa ou houver erro, a conexão é fechada.
     * 
     * @param message Mensagem a ser enviada
     */
    public void sendMessage(Message message) {
        if (!active.get()) {
            return;
        }
        
        try {
            writer.println("MESSAGE:" + message.serialize());
        } catch (Exception e) {
            System.err.println("Error sending message to " + getRemoteIdentifier() + ": " + e.getMessage());
            close();
        }
    }
    
    /**
     * Envia handshake de identificação para o peer remoto.
     * 
     * Usado para identificar este peer após estabelecer conexão.
     * 
     * @param peerName Nome deste peer para identificação
     */
    public void sendHandshake(String peerName) {
        if (!active.get()) {
            return;
        }
        
        try {
            writer.println("HANDSHAKE:" + peerName);
        } catch (Exception e) {
            System.err.println("Error sending handshake to " + getRemoteIdentifier() + ": " + e.getMessage());
            close();
        }
    }
    
    /**
     * Fecha a conexão e libera todos os recursos associados.
     * 
     * Método thread-safe que pode ser chamado múltiplas vezes sem efeito.
     * Notifica o peer pai sobre a desconexão para remoção da lista.
     */
    public void close() {
        if (active.compareAndSet(true, false)) {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (socket != null) socket.close();
                
                System.out.println("Disconnected from peer " + getRemoteIdentifier());
                
                // Notify parent peer about disconnection
                parentPeer.removePeerConnection(this);
                
            } catch (IOException e) {
                System.err.println("Error closing connection to " + getRemoteIdentifier() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica se a conexão está ativa.
     * 
     * Uma conexão é considerada ativa se o flag interno está true
     * E o socket não foi fechado.
     * 
     * @return true se conexão está ativa
     */
    public boolean isActive() {
        return active.get() && !socket.isClosed();
    }
    
    /**
     * Retorna identificador único do peer remoto.
     * 
     * Formato: "enderecoIP:porta"
     * 
     * @return String identificador do peer remoto
     */
    public String getRemoteIdentifier() {
        return remoteAddress + ":" + remotePort;
    }
    
    /**
     * Retorna nome do peer remoto se conhecido.
     * 
     * Retorna o nome obtido via handshake, ou o identificador se
     * o nome ainda não foi recebido.
     * 
     * @return String nome do peer ou identificador
     */
    public String getRemotePeerName() {
        return remotePeerName != null ? remotePeerName : getRemoteIdentifier();
    }
    
    /**
     * Representação string da conexão para debug.
     * 
     * @return String com informações da conexão
     */
    @Override
    public String toString() {
        return "PeerConnection{" +
                "remote=" + getRemoteIdentifier() +
                ", name=" + (remotePeerName != null ? remotePeerName : "unknown") +
                ", active=" + active.get() +
                '}';
    }
}
```

### Message

```java
package com.p2pchat;

/**
 * Representa uma mensagem no sistema de chat P2P.
 * 
 * Encapsula todas as informações necessárias de uma mensagem: remetente,
 * conteúdo e timestamp. Fornece funcionalidades de serialização para
 * transmissão na rede e formatação para exibição.
 * 
 * @author Sistema P2P Chat
 * @version 1.0.0
 * @since 1.0.0
 */
public class Message {
    
    /**
     * Nome do remetente da mensagem.
     * Imutável após criação da mensagem.
     */
    private final String sender;
    
    /**
     * Conteúdo textual da mensagem.
     * Imutável após criação da mensagem.
     */
    private final String content;
    
    /**
     * Timestamp de quando a mensagem foi criada.
     * Definido automaticamente no momento da construção.
     */
    private final LocalDateTime timestamp;
    
    /**
     * Formatador para exibição do timestamp.
     * Formato HH:mm:ss para interface de usuário.
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * Construtor da Message.
     * 
     * Cria uma nova mensagem com timestamp atual. O timestamp é definido
     * automaticamente no momento da construção usando LocalDateTime.now().
     * 
     * @param sender Nome do remetente da mensagem
     * @param content Conteúdo textual da mensagem
     */
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Retorna o nome do remetente.
     * 
     * @return String contendo nome do remetente
     */
    public String getSender() {
        return sender;
    }
    
    /**
     * Retorna o conteúdo da mensagem.
     * 
     * @return String contendo conteúdo da mensagem
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Retorna o timestamp da mensagem.
     * 
     * @return LocalDateTime com timestamp de criação
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Retorna timestamp formatado para exibição.
     * 
     * Usa formato HH:mm:ss para interface de usuário.
     * 
     * @return String com timestamp formatado
     */
    public String getFormattedTime() {
        return timestamp.format(formatter);
    }
    
    /**
     * Serializa a mensagem para transmissão na rede.
     * 
     * Formato: "remetente|conteudo|timestamp_iso"
     * Usa pipe (|) como delimitador entre campos.
     * 
     * @return String serializada para transmissão
     */
    public String serialize() {
        return sender + "|" + content + "|" + timestamp.toString();
    }
    
    /**
     * Deserializa uma mensagem recebida da rede.
     * 
     * Reconstrói objeto Message a partir de string serializada.
     * Por simplicidade, usa timestamp atual em vez de preservar o original.
     * 
     * @param data String serializada recebida da rede
     * @return Message deserializada
     * @throws IllegalArgumentException se formato for inválido
     */
    public static Message deserialize(String data) {
        String[] parts = data.split("\\|", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid message format");
        }
        
        Message msg = new Message(parts[0], parts[1]);
        // We'll use the current time for simplicity, but could parse the timestamp if needed
        return msg;
    }
    
    /**
     * Representação formatada da mensagem para exibição.
     * 
     * Formato: "[HH:mm:ss] Remetente: Conteúdo"
     * 
     * @return String formatada para exibição na interface
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), sender, content);
    }
}
```

### MessageHistory

```java
package com.p2pchat;

/**
 * Gerencia o histórico de mensagens para a sessão de chat P2P.
 * 
 * Implementação thread-safe que armazena mensagens em memória durante
 * a execução da aplicação. Fornece funcionalidades de adição, recuperação
 * e exibição de mensagens com limitação automática de tamanho.
 * 
 * @author Sistema P2P Chat
 * @version 1.0.0
 * @since 1.0.0
 */
public class MessageHistory {
    
    /**
     * Lista thread-safe de mensagens.
     * Usa CopyOnWriteArrayList para permitir acesso concorrente seguro
     * de múltiplas threads sem sincronização externa.
     */
    private final List<Message> messages;
    
    /**
     * Número máximo de mensagens a manter no histórico.
     * Quando excedido, mensagens mais antigas são removidas automaticamente.
     */
    private final int maxMessages;
    
    /**
     * Construtor padrão com limite de 1000 mensagens.
     */
    public MessageHistory() {
        this(1000); // Default maximum of 1000 messages
    }
    
    /**
     * Construtor com limite personalizado de mensagens.
     * 
     * @param maxMessages Número máximo de mensagens a manter
     */
    public MessageHistory(int maxMessages) {
        this.messages = new CopyOnWriteArrayList<>();
        this.maxMessages = maxMessages;
    }
    
    /**
     * Adiciona uma mensagem ao histórico.
     * 
     * Se o número de mensagens exceder o limite máximo, a mensagem
     * mais antiga é removida automaticamente. Operação thread-safe.
     * 
     * @param message Mensagem a ser adicionada
     */
    public void addMessage(Message message) {
        messages.add(message);
        
        // Remove oldest messages if we exceed the limit
        if (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }
    
    /**
     * Retorna todas as mensagens em ordem cronológica.
     * 
     * Retorna uma cópia da lista para evitar modificações externas
     * acidentais do histórico original.
     * 
     * @return Lista imutável de todas as mensagens
     */
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * Retorna as N mensagens mais recentes.
     * 
     * Se solicitadas mais mensagens do que disponíveis, retorna todas.
     * Retorna cópia da sublista para segurança.
     * 
     * @param count Número de mensagens recentes desejadas
     * @return Lista das mensagens mais recentes (máximo count)
     */
    public List<Message> getRecentMessages(int count) {
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }
    
    /**
     * Remove todas as mensagens do histórico.
     * 
     * Operação thread-safe que limpa completamente o histórico.
     */
    public void clear() {
        messages.clear();
    }
    
    /**
     * Retorna o número total de mensagens no histórico.
     * 
     * @return int com número de mensagens
     */
    public int size() {
        return messages.size();
    }
    
    /**
     * Exibe mensagens recentes formatadas no console.
     * 
     * Método de conveniência que recupera e exibe as N mensagens
     * mais recentes com formatação apropriada para a interface.
     * 
     * @param count Número de mensagens recentes a exibir
     */
    public void displayRecentMessages(int count) {
        List<Message> recent = getRecentMessages(count);
        System.out.println("\n--- Recent Messages ---");
        for (Message msg : recent) {
            System.out.println(msg);
        }
        System.out.println("----------------------\n");
    }
}
```

### PeerDiscovery

```java
package com.p2pchat;

/**
 * Mecanismo de descoberta automática de peers na rede local.
 * 
 * Implementa algoritmos de varredura para encontrar outros peers ativos
 * nas portas comuns. Usa multi-threading para eficiência e timeouts
 * para evitar bloqueios longos.
 * 
 * @author Sistema P2P Chat
 * @version 1.0.0
 * @since 1.0.0
 */
public class PeerDiscovery {
    
    /**
     * Array de portas comuns onde peers são tipicamente executados.
     * Essas portas são verificadas durante a descoberta automática.
     */
    private static final int[] COMMON_PORTS = {8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8090};
    
    /**
     * Timeout em milissegundos para cada tentativa de conexão durante descoberta.
     * Valor baixo para tornar a descoberta rápida, mas alto o suficiente para
     * redes locais típicas.
     */
    private static final int DISCOVERY_TIMEOUT_MS = 1000; // 1 second timeout per connection
    
    /**
     * Descobre peers ativos no localhost usando portas comuns.
     * 
     * Implementa descoberta paralela usando ExecutorService para testar
     * múltiplas portas simultaneamente. Cada teste de conexão roda em
     * timeout limitado para evitar bloqueios.
     * 
     * @param excludePort Porta a ser excluída da busca (tipicamente a própria porta)
     * @return Lista de PeerInfo dos peers encontrados
     */
    public static List<PeerInfo> discoverLocalPeers(int excludePort) {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<PeerInfo>> futures = new ArrayList<>();
        
        // Test each common port
        for (int port : COMMON_PORTS) {
            if (port != excludePort) { // Don't try to connect to ourselves
                CompletableFuture<PeerInfo> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Socket testSocket = new Socket();
                        testSocket.connect(new java.net.InetSocketAddress("localhost", port), DISCOVERY_TIMEOUT_MS);
                        testSocket.close();
                        return new PeerInfo("localhost", port);
                    } catch (IOException e) {
                        return null; // No peer at this port
                    }
                }, executor);
                futures.add(future);
            }
        }
        
        // Collect results
        for (CompletableFuture<PeerInfo> future : futures) {
            try {
                PeerInfo peerInfo = future.get(DISCOVERY_TIMEOUT_MS + 500, TimeUnit.MILLISECONDS);
                if (peerInfo != null) {
                    discoveredPeers.add(peerInfo);
                }
            } catch (Exception e) {
                // Ignore failed discoveries
            }
        }
        
        executor.shutdown();
        return discoveredPeers;
    }
    
    /**
     * Descobre peers na rede local (mesma subnet).
     * 
     * Funcionalidade mais avançada que varre a subnet local procurando
     * por peers. Mais lenta que descoberta local mas pode encontrar
     * peers em outras máquinas da rede.
     * 
     * @param excludePort Porta a ser excluída da busca
     * @return Lista de PeerInfo dos peers encontrados na rede
     */
    public static List<PeerInfo> discoverNetworkPeers(int excludePort) {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            String subnet = getSubnet(localAddress.getHostAddress());
            
            ExecutorService executor = Executors.newFixedThreadPool(50);
            List<CompletableFuture<List<PeerInfo>>> futures = new ArrayList<>();
            
            // Scan the subnet (e.g., 192.168.1.1 to 192.168.1.254)
            for (int i = 1; i <= 254; i++) {
                final String targetIP = subnet + i;
                CompletableFuture<List<PeerInfo>> future = CompletableFuture.supplyAsync(() -> {
                    List<PeerInfo> peersAtIP = new ArrayList<>();
                    for (int port : COMMON_PORTS) {
                        if (port != excludePort || !targetIP.equals("localhost")) {
                            try {
                                Socket testSocket = new Socket();
                                testSocket.connect(new java.net.InetSocketAddress(targetIP, port), 500);
                                testSocket.close();
                                peersAtIP.add(new PeerInfo(targetIP, port));
                            } catch (IOException e) {
                                // No peer at this IP:port combination
                            }
                        }
                    }
                    return peersAtIP;
                }, executor);
                futures.add(future);
            }
            
            // Collect results with timeout
            for (CompletableFuture<List<PeerInfo>> future : futures) {
                try {
                    List<PeerInfo> peersAtIP = future.get(2000, TimeUnit.MILLISECONDS);
                    discoveredPeers.addAll(peersAtIP);
                } catch (Exception e) {
                    // Ignore failed discoveries
                }
            }
            
            executor.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error during network discovery: " + e.getMessage());
        }
        
        return discoveredPeers;
    }
    
    /**
     * Extrai subnet de um endereço IP.
     * 
     * Exemplo: "192.168.1.100" -> "192.168.1."
     * Usado para varredura de rede local.
     * 
     * @param ipAddress Endereço IP completo
     * @return String subnet (primeiros 3 octetos + ponto)
     */
    private static String getSubnet(String ipAddress) {
        int lastDot = ipAddress.lastIndexOf('.');
        if (lastDot > 0) {
            return ipAddress.substring(0, lastDot + 1);
        }
        return "192.168.1."; // Default fallback
    }
    
    /**
     * Classe interna que representa informações de um peer descoberto.
     * 
     * Encapsula host e porta de um peer encontrado durante descoberta.
     * Immutable para segurança.
     */
    public static class PeerInfo {
        
        /**
         * Hostname ou endereço IP do peer.
         */
        private final String host;
        
        /**
         * Porta onde o peer está escutando.
         */
        private final int port;
        
        /**
         * Construtor do PeerInfo.
         * 
         * @param host Hostname ou IP do peer
         * @param port Porta do peer
         */
        public PeerInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
        
        /**
         * Retorna o hostname/IP do peer.
         * 
         * @return String com host
         */
        public String getHost() {
            return host;
        }
        
        /**
         * Retorna a porta do peer.
         * 
         * @return int com porta
         */
        public int getPort() {
            return port;
        }
        
        /**
         * Representação string no formato host:port.
         * 
         * @return String formatada "host:port"
         */
        @Override
        public String toString() {
            return host + ":" + port;
        }
        
        /**
         * Implementação de equals baseada em host e porta.
         * 
         * @param obj Objeto a comparar
         * @return true se host e porta são iguais
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            PeerInfo peerInfo = (PeerInfo) obj;
            return port == peerInfo.port && host.equals(peerInfo.host);
        }
        
        /**
         * Hash code baseado em host e porta.
         * 
         * @return int hash code
         */
        @Override
        public int hashCode() {
            return host.hashCode() * 31 + port;
        }
    }
}
```

## Padrões de Design Utilizados

### 1. Observer Pattern
- **PeerConnection** notifica **Peer** sobre mensagens recebidas
- **Peer** notifica interface sobre novas mensagens

### 2. Thread-Safe Singleton (para MessageHistory)
- Uso de `CopyOnWriteArrayList` para acesso concorrente seguro
- `AtomicBoolean` para flags de controle thread-safe

### 3. Factory Method Pattern
- `Message.deserialize()` atua como factory method para criar Messages

### 4. Command Pattern
- `ChatClient.processCommand()` implementa processamento de comandos

### 5. Resource Management Pattern
- Try-with-resources implícito nos streams
- Cleanup automático em `PeerConnection.close()`

## Considerações de Performance

### Threading
- **Vantagens**: Cada conexão em thread separada evita bloqueios
- **Desvantagens**: Overhead de threads com muitas conexões
- **Alternativa**: NIO para aplicações de alta escala

### Estruturas de Dados
- **CopyOnWriteArrayList**: Otimizada para leitura frequente, escrita ocasional
- **AtomicBoolean**: Lock-free para flags simples
- **BufferedReader/PrintWriter**: Buffering para eficiência de I/O

### Network Protocol
- **Vantagens**: Protocolo textual simples de debugar
- **Desvantagens**: Overhead de serialização/parsing
- **Alternativa**: Protocolo binário para performance

## Tratamento de Erros

### IOException
- Conexões de rede podem falhar a qualquer momento
- Cleanup automático em caso de erro
- Logging apropriado para diagnóstico

### IllegalArgumentException  
- Validação de formato de mensagens
- Dados corrompidos ou malformados

### NumberFormatException
- Entrada de usuário para portas
- Validação e retry automático

## Extensibilidade

### Novas Funcionalidades
1. **Criptografia**: Modificar `PeerConnection` para usar SSL/TLS
2. **Persistência**: Estender `MessageHistory` com storage
3. **GUI**: Substituir `ChatClient` por interface gráfica
4. **Roteamento**: Implementar algoritmos de roteamento mais sofisticados

### Pontos de Extensão
- `Message`: Adicionar novos tipos de mensagem
- `PeerDiscovery`: Novos algoritmos de descoberta
- `Peer`: Novos protocolos de comunicação
- `MessageHistory`: Diferentes estratégias de storage

## Limitações da API

### Segurança
- Sem criptografia de mensagens
- Sem autenticação forte
- Vulnerável a ataques de rede local

### Escalabilidade
- Número limitado de conexões simultâneas
- Broadcasting pode ser ineficiente
- Sem otimizações para WANs

### Funcionalidade
- Apenas texto (sem arquivos/mídia)
- Sem persistência entre sessões
- Protocolo simples sem extensões

---

Esta documentação API fornece o detalhamento completo de todas as classes, métodos e funcionalidades do sistema, servindo como referência técnica para desenvolvedores que trabalham com o código.