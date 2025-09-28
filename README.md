# Sistema Distribuído de Análise de Imagens com IA REAL - Trabalho 6 SD

Sistema distribuído em Java com containers Docker, RabbitMQ e **IA real embutida** nos consumidores para processamento e análise visual de imagens usando computer vision.

## 🏗️ Arquitetura do Sistema

O sistema é composto por 4 containers principais:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Gerador de     │    │                 │    │ Consumidor      │
│  Mensagens      │───▶│    RabbitMQ     │───▶│ Análise de      │
│  (6 msgs/seg)   │    │                 │    │ Sentimento      │
└─────────────────┘    │  Topic Exchange │    │ (Smile ML)      │
                       │                 │    └─────────────────┘
                       │   face_queue    │
                       │   team_queue    │    ┌─────────────────┐
                       │                 │    │ Consumidor      │
                       │                 │───▶│ Identificação   │
                       └─────────────────┘    │ de Times        │
                                              │ (Smile ML)      │
                                              └─────────────────┘
```

## 🚀 Componentes

### 1. **Gerador de Mensagens**
- **Função**: Gera carga constante de mensagens (6 mensagens/segundo)
- **Tipos de Imagem**: 
  - 60% rostos de pessoas (routing key: `face`)
  - 40% brasões de times de futebol (routing key: `team`)
- **Tecnologias**: Java 11, RabbitMQ Client, Jackson JSON

### 2. **RabbitMQ Broker**
- **Exchange**: Topic (`image_analysis_exchange`)
- **Filas**: 
  - `face_queue` (rostos)
  - `team_queue` (times)
- **Interface Admin**: http://localhost:15672 (admin/admin123)
- **Configuração**: Pré-configurado com definições JSON

### 3. **Consumidor de Análise de Sentimento (IA REAL)**
- **Função**: Processa imagens de rostos com **análise visual real**
- **IA**: Algoritmos de computer vision nativos em Java (BufferedImage, AWT)
- **Análise Real**: 
  - Brilho médio pixel-a-pixel
  - Saturação de cores 
  - Contraste e histograma
  - Detecção de cores quentes (felicidade)
- **Tempo**: 2-4 segundos por mensagem
- **Saída**: FELIZ/TRISTE com características visuais detalhadas

### 4. **Consumidor de Identificação de Times (IA REAL)**
- **Função**: Identifica brasões através de **análise de cores dominantes**  
- **IA**: Computer vision para extração de características reais
- **Análise Real**:
  - % pixels vermelhos, verdes, azuis
  - % pixels pretos e brancos  
  - Complexidade visual (variação de cores)
  - Classificação por regras de cor
- **Tempo**: 3-5 segundos por mensagem
- **Base**: 8 times com regras de cores específicas
- **Saída**: Nome do time com score de correspondência visual

## 🛠️ Tecnologias Utilizadas

- **Java 11** - Linguagem de programação
- **Maven** - Gerenciamento de dependências  
- **Docker & Docker Compose** - Containerização
- **RabbitMQ** - Message Broker
- **Java AWT/BufferedImage** - **Processamento real de imagem**
- **Computer Vision** - **Algoritmos nativos de análise visual**
- **Jackson** - Processamento JSON
- **Python + Pillow** - Gerador de imagens de teste

## 📋 Pré-requisitos

- Docker Desktop instalado
- Docker Compose disponível
- Portas 5672 e 15672 livres

## 🎯 Como Executar

### ⚠️ Pré-requisitos
1. **Inicie o Docker Desktop** antes de executar o sistema
2. Aguarde o Docker estar completamente carregado
3. Verifique se as portas 5672 e 15672 estão livres

### Opção 1: Script Automatizado (Windows)
```batch
iniciar_sistema.bat
```

### Opção 2: Script Automatizado (Linux/Mac)
```bash
chmod +x iniciar_sistema.sh
./iniciar_sistema.sh
```

### Opção 3: Comandos Manuais
```bash
# 1. Criar imagens de teste (opcional)
pip install pillow
python criar_imagens_teste.py

# 2. Iniciar sistema Docker
docker-compose down
docker-compose up --build
```

## 📊 Monitoramento

### Interface RabbitMQ
- **URL**: http://localhost:15672
- **Usuário**: admin
- **Senha**: admin123

### Logs dos Containers
```bash
# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs específicos
docker-compose logs -f gerador-mensagens
docker-compose logs -f consumidor-sentimento
docker-compose logs -f consumidor-times
```

## 📈 Comportamento Esperado

### 1. **Geração de Carga**
- 6 mensagens por segundo
- Alternância entre rostos (60%) e times (40%)
- Mensagens com dados simulados de imagem

### 2. **Acúmulo nas Filas**
- As filas devem crescer visivelmente no RabbitMQ Admin
- Consumidores processam mais lentamente que a geração
- Demonstra o conceito de backpressure

### 3. **Processamento com IA REAL**
- **Sentimento**: Análise pixel-a-pixel das imagens + nome do arquivo
- **Times**: Classificação por cores dominantes + padrões visuais reais
- Ambos usam computer vision nativo do Java (sem bibliotecas externas)

### 4. **Logs Informativos com Análise Real**
```
[SENTIMENTO] Processando: pessoa_feliz_1.jpg
[SENTIMENTO] ✓ Resultado: FELIZ (89% confiança) (2.3s)
Análise Visual Real - Brilho: 0.72, Saturação: 0.65, Contraste: 0.41, Cores quentes: 45%

[TIMES] Processando: logo_flamengo_1.jpg  
[TIMES] ⚽ Identificado: FLAMENGO - RJ (87% confiança) (4.1s)
Classificação visual por cores - Score: 0.89 (R:67%, V:12%, A:8%)
```

## 🔧 Configurações

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| RABBITMQ_HOST | localhost | Host do RabbitMQ |
| RABBITMQ_PORT | 5672 | Porta do RabbitMQ |
| RABBITMQ_USER | admin | Usuário do RabbitMQ |
| RABBITMQ_PASS | admin123 | Senha do RabbitMQ |

### Taxas de Processamento

- **Gerador**: 6 mensagens/segundo
- **Consumidor Sentimento**: 2-4 segundos/mensagem
- **Consumidor Times**: 3-5 segundos/mensagem

## 🐛 Solução de Problemas

### Container não inicia
```bash
# Verificar logs
docker-compose logs [nome-do-serviço]

# Reconstruir do zero
docker-compose down
docker system prune -f
docker-compose up --build
```

### RabbitMQ não conecta
- Aguardar 30-60 segundos após o start (healthcheck)
- Verificar se a porta 5672 está livre
- Checar logs do container rabbitmq

### Biblioteca Smile não encontrada
- O Maven baixa automaticamente durante o build
- Em caso de erro, limpar cache: `docker system prune -f`

## 📁 Estrutura do Projeto

```
sistema-carga-ia/
├── docker-compose.yml              # Orquestração dos containers
├── start.bat / start.sh           # Scripts de inicialização
├── README.md                      # Documentação
├── gerador-mensagens/             # Serviço gerador
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/exemplo/
├── consumidor-sentimento/         # IA análise de sentimento
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/exemplo/
├── consumidor-times/              # IA identificação de times
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/exemplo/
├── rabbitmq-setup/                # Configurações RabbitMQ
│   ├── definitions.json
│   └── rabbitmq.conf
└── images/                        # Placeholder para imagens
    ├── faces/
    └── teams/
```

## 🎓 Características Técnicas

### Conformidade com Requisitos
- ✅ 4 containers (RabbitMQ + Gerador + 2 Consumidores)
- ✅ Geração de carga rápida (6 msgs/seg)
- ✅ RabbitMQ com Topic Exchange
- ✅ Routing keys adequadas (face/team)
- ✅ Interface de administração habilitada
- ✅ Processamento lento para visualizar acúmulo
- ✅ IA embutida com biblioteca Smile
- ✅ Network compartilhada entre containers

### Implementações de IA

#### Análise de Sentimento
- Extração de características matemáticas da imagem
- Análise estatística com Smile (média, desvio padrão)
- Classificação baseada em nome + características
- Probabilidade de felicidade com ruído gaussiano

#### Identificação de Times
- Simulação de extração de características visuais (cores)
- Classificação por similaridade usando distância euclidiana
- Base de conhecimento de 10 times brasileiros
- Cálculo de confiança baseado em score de similaridade

## 👥 Autor

Jonas - Sistemas Distribuídos - Trabalho 6

---

*Sistema desenvolvido para demonstrar conceitos de sistemas distribuídos, containerização e integração de IA em arquiteturas de microsserviços.*