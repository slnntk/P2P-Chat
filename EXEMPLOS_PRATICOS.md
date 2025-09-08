# Exemplos Práticos e Cenários de Uso - Sistema de Chat P2P

## Índice
1. [Cenários Básicos de Uso](#cenários-básicos-de-uso)
2. [Configurações de Rede](#configurações-de-rede)
3. [Testes e Validação](#testes-e-validação)
4. [Solução Detalhada de Problemas](#solução-detalhada-de-problemas)
5. [Scripts de Automação](#scripts-de-automação)
6. [Casos de Uso Avançados](#casos-de-uso-avançados)

## Cenários Básicos de Uso

### Cenário 1: Chat Simples Entre Duas Pessoas

#### Objetivo
Estabelecer comunicação básica entre dois usuários na mesma máquina.

#### Configuração
```bash
# Terminal 1 - Usuário Alice
./run.sh
Enter your username: Alice
Enter listening port (8080-8090): 8080

# Terminal 2 - Usuário Bob  
./run.sh
Enter your username: Bob
Enter listening port (8080-8090): 8081
```

#### Sequência de Conexão
**Terminal 1 (Alice):**
```
> status
=== Peer Status ===
Name: Alice
Listening Port: 8080
Active Connections: 0
Messages in History: 0
==================

> # Aguarda conexão de Bob
```

**Terminal 2 (Bob):**
```
> connect localhost:8080
Connected to peer at localhost:8080
Peer Alice (localhost:8080) connected

> Oi Alice! Consegui conectar!
[14:30:15] Bob: Oi Alice! Consegui conectar!
```

**Terminal 1 (Alice) - recebe automaticamente:**
```
New peer connected from 127.0.0.1:xxxxx
Peer Bob (127.0.0.1:xxxxx) connected
[14:30:15] Bob: Oi Alice! Consegui conectar!

> Oi Bob! Bem-vindo ao chat!
[14:30:20] Alice: Oi Bob! Bem-vindo ao chat!
```

#### Verificação de Status
```bash
> status
=== Peer Status ===
Name: Alice
Listening Port: 8080
Active Connections: 1
  - Bob
Messages in History: 2
==================
```

### Cenário 2: Rede de Três Usuários com Descoberta Automática

#### Objetivo
Demonstrar descoberta automática e broadcasting em rede de múltiplos peers.

#### Configuração Inicial
```bash
# Terminal 1 - Alice (Hub Central)
./run.sh
Enter your username: Alice
Enter listening port (8080-8090): 8080

# Terminal 2 - Bob
./run.sh
Enter your username: Bob
Enter listening port (8080-8090): 8081

# Terminal 3 - Carol
./run.sh
Enter your username: Carol
Enter listening port (8080-8090): 8082
```

#### Estabelecimento da Rede

**Passo 1: Bob conecta à Alice**
```bash
# Terminal 2 (Bob)
> connect localhost:8080
Connected to peer at localhost:8080
Peer Alice (localhost:8080) connected
```

**Passo 2: Carol usa descoberta automática**
```bash
# Terminal 3 (Carol)
> discover
Discovering peers...
Found 2 peer(s):
1. localhost:8080
2. localhost:8081
Enter number to connect (or 0 to cancel): 1
Connected to peer at localhost:8080
Peer Alice (localhost:8080) connected

> connect localhost:8081
Connected to peer at localhost:8081
Peer Bob (localhost:8081) connected
```

#### Teste de Broadcasting

**Carol envia mensagem:**
```bash
# Terminal 3 (Carol)
> Olá pessoal! Consegui me conectar a todos!
[14:35:10] Carol: Olá pessoal! Consegui me conectar a todos!
```

**A mensagem aparece em todos os terminais:**
```bash
# Terminal 1 (Alice)
[14:35:10] Carol: Olá pessoal! Consegui me conectar a todos!

# Terminal 2 (Bob)  
[14:35:10] Carol: Olá pessoal! Consegui me conectar a todos!
```

#### Validação da Topologia
```bash
# Em qualquer terminal
> status
=== Peer Status ===
Name: Carol
Listening Port: 8082
Active Connections: 2
  - Alice
  - Bob
Messages in History: 1
==================
```

### Cenário 3: Simulação de Sala de Chat com 4 Usuários

#### Configuração Completa
```bash
# Terminal 1 - Alice (Coordenador)
./run.sh
Username: Alice, Port: 8080

# Terminal 2 - Bob
./run.sh  
Username: Bob, Port: 8081

# Terminal 3 - Carol
./run.sh
Username: Carol, Port: 8082

# Terminal 4 - Dave
./run.sh
Username: Dave, Port: 8083
```

#### Estratégia de Conexão em Estrela
Todos se conectam à Alice (hub central):

```bash
# Bob conecta à Alice
> connect localhost:8080

# Carol conecta à Alice  
> connect localhost:8080

# Dave conecta à Alice
> connect localhost:8080
```

#### Conversação Simulada
```bash
# Alice inicia conversa
> Bem-vindos ao chat! Vamos nos apresentar?

# Bob responde
> Oi pessoal! Sou Bob, desenvolvedor Java

# Carol se apresenta
> Olá! Sou Carol, estudante de TI

# Dave participa
> Oi galera! Dave aqui, especialista em redes

# Alice pergunta
> Que legal! Alguém já trabalhou com P2P antes?

# Carol responde
> Essa é minha primeira experiência prática!

# Dave comenta
> Já usei BitTorrent, mas implementar é diferente

# Bob adiciona
> O protocolo é bem simples, mas eficaz
```

#### Demonstração de Comandos Úteis
```bash
# Verificar histórico da conversa
> history 5
--- Recent Messages ---
[14:40:15] Alice: Bem-vindos ao chat! Vamos nos apresentar?
[14:40:22] Bob: Oi pessoal! Sou Bob, desenvolvedor Java
[14:40:28] Carol: Olá! Sou Carol, estudante de TI
[14:40:35] Dave: Oi galera! Dave aqui, especialista em redes
[14:40:42] Alice: Que legal! Alguém já trabalhou com P2P antes?
----------------------

# Status completo da rede
> status
=== Peer Status ===
Name: Alice
Listening Port: 8080
Active Connections: 3
  - Bob
  - Carol  
  - Dave
Messages in History: 8
==================
```

## Configurações de Rede

### Configuração para Rede Local (LAN)

#### Cenário: Múltiplas Máquinas na Mesma Rede

**Máquina 1 (192.168.1.100) - Alice:**
```bash
./run.sh
Username: Alice
Port: 8080

> status
Listening on: 192.168.1.100:8080
```

**Máquina 2 (192.168.1.101) - Bob:**
```bash
./run.sh
Username: Bob  
Port: 8080  # Pode usar mesma porta (máquinas diferentes)

> connect 192.168.1.100:8080
Connected to peer at 192.168.1.100:8080
```

**Máquina 3 (192.168.1.102) - Carol:**
```bash
./run.sh
Username: Carol
Port: 8080

# Usar descoberta de rede (se implementada)
> discover
# Ou conectar manualmente
> connect 192.168.1.100:8080
> connect 192.168.1.101:8080
```

### Configuração com Firewall

#### Liberação de Portas no Linux
```bash
# UFW (Ubuntu/Debian)
sudo ufw allow 8080:8090/tcp
sudo ufw status

# iptables (genérico)
sudo iptables -A INPUT -p tcp --dport 8080:8090 -j ACCEPT

# firewalld (CentOS/RHEL)
sudo firewall-cmd --permanent --add-port=8080-8090/tcp
sudo firewall-cmd --reload
```

#### Liberação no Windows
```cmd
# Prompt de comando como administrador
netsh advfirewall firewall add rule name="P2P Chat" dir=in action=allow protocol=TCP localport=8080-8090
```

### Teste de Conectividade de Rede

#### Verificação Manual de Portas
```bash
# Testar se peer está ouvindo
telnet localhost 8080
# Se conectar, peer está ativo

# Verificar portas abertas
netstat -ln | grep 808
# Deve mostrar LISTEN nas portas dos peers

# Teste de conectividade remota
telnet 192.168.1.100 8080
```

#### Script de Teste de Conectividade
```bash
#!/bin/bash
# test_connectivity.sh

echo "Testando conectividade P2P..."

PORTS=(8080 8081 8082 8083 8084 8085)
HOST=${1:-localhost}

for port in "${PORTS[@]}"; do
    echo -n "Testando $HOST:$port... "
    if timeout 2 bash -c "</dev/tcp/$HOST/$port" 2>/dev/null; then
        echo "ATIVO"
    else
        echo "inativo"
    fi
done
```

## Testes e Validação

### Teste de Carga Básico

#### Cenário: Múltiplas Mensagens Rápidas
```bash
# Terminal Alice
> Mensagem 1
> Mensagem 2
> Mensagem 3
> status  # Verificar se todas chegaram ao histórico
```

#### Script de Teste Automatizado
```bash
#!/bin/bash
# load_test.sh - Teste básico de carga

echo "Alice" | java -jar p2pchat.jar <<EOF &
8080
status
Mensagem de teste 1
Mensagem de teste 2  
Mensagem de teste 3
history
quit
EOF

sleep 2

echo "Bob" | java -jar p2pchat.jar <<EOF &
8081
connect localhost:8080
Resposta do Bob 1
Resposta do Bob 2
status
history
quit
EOF

wait
echo "Teste de carga concluído"
```

### Teste de Robustez

#### Cenário: Desconexões Abruptas
```bash
# Terminal 1 - Alice
> Iniciando teste de robustez...

# Terminal 2 - Bob (será desconectado abruptamente)
> connect localhost:8080
> Conectado para teste

# Simular desconexão abrupta (Ctrl+C no Terminal 2)

# Terminal 1 - Alice (deve detectar desconexão)
> status  # Deve mostrar 0 conexões ativas
> Testando após desconexão do Bob
```

#### Teste de Reconexão
```bash
# Após desconexão, Bob reconecta
# Terminal 2 - Bob (reiniciado)
./run.sh
Username: Bob
Port: 8081
> connect localhost:8080
> Reconectei após falha
```

### Teste de Broadcasting

#### Cenário: Verificação de Propagação
```bash
# Configuração: A ↔ B ↔ C (cadeia linear)

# Terminal A
> connect localhost:8081  # Conecta ao B

# Terminal B  
# Aceita conexão de A automaticamente
> connect localhost:8082  # Conecta ao C

# Terminal C
# Aceita conexão de B automaticamente

# Teste de propagação
# Terminal A envia mensagem
> Mensagem originada em A

# Verificar se chegou em todos:
# Terminal B: deve mostrar a mensagem
# Terminal C: deve mostrar a mensagem via B
```

## Solução Detalhada de Problemas

### Problema 1: "Address Already in Use"

#### Diagnóstico
```bash
# Identificar processo usando a porta
lsof -i :8080
netstat -tulpn | grep 8080

# Output esperado:
# java    12345 user   10u  IPv6  123456      0t0  TCP *:8080 (LISTEN)
```

#### Soluções

**Opção 1: Matar processo anterior**
```bash
# Encontrar PID do processo
ps aux | grep "p2pchat\|ChatClient"

# Matar processo específico
kill -9 <PID>

# Ou matar todos os processos Java (cuidado!)
killall java
```

**Opção 2: Usar porta diferente**
```bash
./run.sh
Enter listening port (8080-8090): 8081  # Tentar porta diferente
```

**Opção 3: Aguardar liberação automática**
```bash
# Aguardar timeout do SO (geralmente 1-2 minutos)
sleep 120
./run.sh
```

### Problema 2: "Connection Refused"

#### Diagnóstico Detalhado
```bash
# 1. Verificar se peer de destino está rodando
telnet localhost 8080
# Se falhar: "Connection refused" = peer não está rodando
# Se conectar: peer está ativo

# 2. Verificar conectividade de rede
ping localhost
ping 192.168.1.100  # Se peer remoto

# 3. Verificar firewall
sudo ufw status  # Ubuntu
sudo iptables -L  # Genérico
```

#### Soluções Passo a Passo

**Passo 1: Verificar se peer de destino está ativo**
```bash
# No peer de destino
> status
# Deve mostrar "Listening Port: XXXX"
```

**Passo 2: Testar conectividade básica**
```bash
# Teste manual de conexão
telnet <host> <port>

# Se telnet funciona mas chat não:
# Problema no código/protocolo

# Se telnet falha:
# Problema de rede/firewall
```

**Passo 3: Usar descoberta automática**
```bash
# Em vez de conectar manualmente
> discover
# Se não encontrar peers, problema de rede
# Se encontrar mas não conectar, problema de protocolo
```

### Problema 3: Mensagens Não Propagam

#### Diagnóstico
```bash
# Verificar topologia da rede
# Em cada peer:
> status

# Verificar histórico
> history 10

# Enviar mensagem de teste
> TESTE_PROPAGACAO_<NOME_DO_PEER>
```

#### Análise de Topologia
```bash
# Cenário problemático:
# A ↔ B    C ↔ D  (duas redes separadas)
# 
# Solução: conectar as redes
# B conecta a C ou D

# Verificar se há isolamento:
> status
# Se alguns peers não aparecem em "Active Connections"
# Há particionamento da rede
```

#### Soluções

**Solução 1: Reconectar peers isolados**
```bash
# Identificar peer "ponte" central
# Conectar peers isolados ao peer central

# Peer isolado:
> connect <peer_central_host>:<port>
```

**Solução 2: Verificar logs de erro**
```bash
# Observar console para mensagens como:
# "Error sending message to..."
# "Disconnected from peer..."

# Se houver erros de sending:
# Problema na conexão TCP

# Se houver desconexões frequentes:
# Problema de rede ou recursos
```

### Problema 4: Performance Lenta

#### Diagnóstico de Performance
```bash
# Teste de latência de mensagem
# Terminal A:
> TIMESTAMP_$(date +%s)

# Verificar quanto tempo leva para aparecer em outros peers

# Teste de throughput
# Enviar múltiplas mensagens rapidamente:
> msg1
> msg2  
> msg3
# Verificar se todas chegam e em ordem
```

#### Otimizações

**Reduzir Número de Conexões por Peer**
```bash
# Em vez de malha completa (todos conectados a todos):
# A ↔ B ↔ C ↔ D ↔ A

# Usar topologia em árvore ou estrela:
#     B
#     ↕  
# C ↔ A ↔ D
```

**Verificar Recursos do Sistema**
```bash
# Verificar uso de CPU e memória
top | grep java
ps aux | grep ChatClient

# Verificar conexões de rede
netstat -an | grep ESTABLISHED | wc -l
```

### Problema 5: "No Peers Found" na Descoberta

#### Diagnóstico
```bash
# Verificar se outros peers estão nas portas padrão
for port in {8080..8090}; do
    echo -n "Port $port: "
    nc -z localhost $port && echo "OPEN" || echo "CLOSED"
done
```

#### Soluções

**Solução 1: Iniciar peers nas portas corretas**
```bash
# Certificar que peers usam portas 8080-8090
./run.sh
Port: 8080  # Dentro da faixa de descoberta

# Verificar array COMMON_PORTS no código se necessário
```

**Solução 2: Descoberta manual**
```bash
# Se descoberta automática falha, usar conexão manual
> connect localhost:8080
> connect localhost:8081
> connect localhost:8082
```

**Solução 3: Verificar implementação de descoberta**
```bash
# Se usando rede remota, verificar se discoverNetworkPeers está implementado
# Atualmente só discoverLocalPeers está totalmente funcional
```

## Scripts de Automação

### Script de Inicialização Múltipla

```bash
#!/bin/bash
# start_multiple_peers.sh - Inicia múltiplos peers automaticamente

PEERS=("Alice:8080" "Bob:8081" "Carol:8082" "Dave:8083")
PIDS=()

echo "Iniciando peers P2P..."

for peer in "${PEERS[@]}"; do
    name=$(echo $peer | cut -d: -f1)
    port=$(echo $peer | cut -d: -f2)
    
    echo "Iniciando $name na porta $port..."
    
    # Inicia peer em background
    echo -e "$name\n$port" | java -jar p2pchat.jar > "log_$name.txt" 2>&1 &
    pid=$!
    PIDS+=($pid)
    
    echo "$name iniciado com PID $pid"
    sleep 2
done

echo "Todos os peers iniciados!"
echo "PIDs: ${PIDS[@]}"
echo "Para parar todos: kill ${PIDS[@]}"

# Salvar PIDs para cleanup posterior  
echo "${PIDS[@]}" > peer_pids.txt
```

### Script de Teste de Conectividade Automático

```bash
#!/bin/bash
# auto_connect_test.sh - Testa conectividade automaticamente

echo "Teste automático de conectividade P2P"

# Função para testar conexão entre dois peers
test_connection() {
    local from_port=$1
    local to_port=$2
    
    echo "Testando conexão $from_port -> $to_port..."
    
    # Simular comando connect via expect ou timeout
    timeout 5 bash -c "
        echo 'connect localhost:$to_port' | nc localhost $(($from_port + 1000))
    " 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "✓ Conexão $from_port -> $to_port: SUCESSO"
        return 0
    else
        echo "✗ Conexão $from_port -> $to_port: FALHOU"
        return 1
    fi
}

# Testar todas as combinações
PORTS=(8080 8081 8082)
SUCCESS_COUNT=0
TOTAL_TESTS=0

for from_port in "${PORTS[@]}"; do
    for to_port in "${PORTS[@]}"; do
        if [ $from_port -ne $to_port ]; then
            test_connection $from_port $to_port
            if [ $? -eq 0 ]; then
                ((SUCCESS_COUNT++))
            fi
            ((TOTAL_TESTS++))
        fi
    done
done

echo "Resultado: $SUCCESS_COUNT/$TOTAL_TESTS conexões bem-sucedidas"
```

### Script de Cleanup

```bash
#!/bin/bash
# cleanup_peers.sh - Para todos os peers e limpa recursos

echo "Parando todos os peers P2P..."

# Parar por PIDs salvos
if [ -f "peer_pids.txt" ]; then
    PIDS=$(cat peer_pids.txt)
    echo "Parando PIDs: $PIDS"
    kill $PIDS 2>/dev/null
    rm peer_pids.txt
fi

# Parar todos os processos Java do P2P Chat
JAVA_PIDS=$(ps aux | grep "[C]hatClient\|p2pchat\.jar" | awk '{print $2}')
if [ ! -z "$JAVA_PIDS" ]; then
    echo "Parando processos Java restantes: $JAVA_PIDS"
    kill $JAVA_PIDS
fi

# Aguardar e forçar se necessário
sleep 3
REMAINING=$(ps aux | grep "[C]hatClient\|p2pchat\.jar" | awk '{print $2}')
if [ ! -z "$REMAINING" ]; then
    echo "Forçando parada de processos restantes: $REMAINING"
    kill -9 $REMAINING
fi

# Limpar arquivos de log
rm -f log_*.txt

echo "Cleanup concluído!"
```

## Casos de Uso Avançados

### Caso 1: Simulação de Falhas de Rede

#### Objetivo
Testar resiliência do sistema a falhas de rede.

#### Implementação
```bash
# Terminal 1 - Peer Central (Alice)
./run.sh
Username: Alice
Port: 8080

# Terminals 2-4 - Peers conectados
# Cada um conecta à Alice

# Simular falha de rede:
# 1. Desconectar cabo/WiFi por alguns segundos
# 2. Usar iptables para bloquear tráfego temporariamente
sudo iptables -A OUTPUT -p tcp --dport 8080 -j DROP
sleep 10
sudo iptables -D OUTPUT -p tcp --dport 8080 -j DROP

# Observar comportamento:
# - Detecção de desconexão
# - Tentativas de reconexão
# - Perda de mensagens
```

### Caso 2: Teste de Escalabilidade

#### Objetivo
Determinar limites práticos do sistema.

#### Configuração
```bash
# Script para iniciar N peers
#!/bin/bash
N=${1:-10}  # Padrão 10 peers

for i in $(seq 1 $N); do
    port=$((8079 + $i))
    name="Peer$i"
    
    echo -e "$name\n$port" | java -jar p2pchat.jar &
    sleep 1
done

# Conectar todos ao Peer1 (estrela)
# Medir:
# - Tempo de propagação de mensagens
# - Uso de CPU/memória
# - Limite antes de falhas
```

### Caso 3: Implementação de Rooms/Canais

#### Conceito
Embora não implementado nativamente, simular rooms através de convenções.

#### Implementação
```bash
# Convenção: prefixo [ROOM] nas mensagens
> [TECH] Alguém conhece Java NIO?
> [GAMES] Quem joga Among Us?
> [GENERAL] Oi pessoal!

# Filtros no lado do cliente:
# Modificar ChatClient para filtrar por room
# Implementar comando: /join TECH
```

### Caso 4: Monitoramento de Rede P2P

#### Script de Monitoramento
```bash
#!/bin/bash
# monitor_p2p.sh - Monitora rede P2P

while true; do
    echo "=== Status da Rede P2P $(date) ==="
    
    # Contar peers ativos
    active_ports=$(netstat -ln | grep ":808[0-9].*LISTEN" | wc -l)
    echo "Peers ativos: $active_ports"
    
    # Contar conexões estabelecidas
    connections=$(netstat -an | grep ":808[0-9].*ESTABLISHED" | wc -l)
    echo "Conexões ativas: $connections"
    
    # Uso de recursos
    cpu_usage=$(ps aux | grep "[C]hatClient" | awk '{sum += $3} END {print sum}')
    echo "CPU total: ${cpu_usage:-0}%"
    
    memory_usage=$(ps aux | grep "[C]hatClient" | awk '{sum += $4} END {print sum}')
    echo "Memória total: ${memory_usage:-0}%"
    
    echo "================================"
    sleep 10
done
```

### Caso 5: Backup e Recuperação de Estado

#### Conceito
Implementar persistência básica de conexões.

#### Script de Backup
```bash
#!/bin/bash
# backup_connections.sh

echo "Fazendo backup das conexões P2P..."

# Salvar portas ativas
netstat -ln | grep ":808[0-9].*LISTEN" | \
    sed 's/.*:\([0-9]*\).*/\1/' > active_ports.backup

# Salvar conexões estabelecidas
netstat -an | grep ":808[0-9].*ESTABLISHED" | \
    awk '{print $4 " -> " $5}' > active_connections.backup

echo "Backup salvo em:"
echo "- active_ports.backup"  
echo "- active_connections.backup"
```

#### Script de Recuperação
```bash
#!/bin/bash
# restore_connections.sh

if [ ! -f "active_ports.backup" ]; then
    echo "Arquivo de backup não encontrado!"
    exit 1
fi

echo "Restaurando peers..."

# Reiniciar peers nas portas salvas
while read port; do
    echo "Reiniciando peer na porta $port..."
    echo -e "RestoredPeer$port\n$port" | java -jar p2pchat.jar &
    sleep 2
done < active_ports.backup

echo "Peers restaurados! Reconecte manualmente conforme necessário."
```

---

Esta documentação de exemplos práticos fornece cenários reais de uso, scripts de automação e soluções detalhadas para os problemas mais comuns encontrados ao usar o sistema de chat P2P. Os scripts podem ser adaptados conforme necessário para diferentes ambientes e casos de uso específicos.