# XMPP BDI-agent Architecture (XMPPAgent)

## Sobre o Projeto
**XMPPAgent** é uma arquitetura estendida para agentes BDI desenvolvidos através da linguagem AgentSpeak e da plataforma [Jason](https://github.com/jason-lang/jason). O projeto fornece a capacidade para que agentes do Jason enviem e recebam mensagens através do protocolo **XMPP** (Extensible Messaging and Presence Protocol), uma das tecnologias padrão abertas mais antigas e utilizadas para troca de mensagens descentralizadas.

A implementação tira vantagem da biblioteca [Smack](https://www.igniterealtime.org/projects/smack/), um cliente XMPP leve e de código aberto para Java.

## O que ele se propõe a resolver?
Sistemas Multiagentes (SMA) tradicionais desenvolvidos em Jason geralmente comunicam-se de forma fechada e local entre os agentes do mesmo ambiente.
O `XMPPAgent` atua como um *middleware*, fornecendo uma infraestrutura (através do XMPP) capaz de:
- Permitir que os Agentes se comuniquem com outros agentes de SMAs distintos.
- Permitir uma comunicação humano-agente fácil, em que usuários podem enviar mensagens diretamente para o agente usando clientes de chat padrão do mercado (como Pidgin, Gajim, ou ferramentas de celular que possuem suporte ao protocolo XMPP/Jabber).
- Possibilitar sistemas verdadeiramente distribuídos, já que o XMPP permite a comunicação transparente entre instâncias de SMA em qualquer lugar da rede global de internet.

Video demonstrativo utilizando o XMPP em SMA: https://www.youtube.com/watch?v=shzmTHl-tvQ

## Estrutura do Projeto / Como Funciona
O pacote foi construído substituindo a arquitetura padrão do Jason (`AgArch`) pela customizada (`XMPPAgent`), onde implementou-se o método `checkMail()` para procurar por novas mensagens oriundas de chat. Toda mensagem possui uma Força Ilocutória (KQML), por ex: `#tell conteúdo`.

O projeto estende a biblioteca padrão do Jason dispondo novas *internal actions*:
1. `xmpp.credentials("user@dominio.com", "senha")`: Configura as credenciais de autenticação no servidor XMPP.
2. `xmpp.chatService("host_ou_ip", porta)`: Configura o serviço e solicita a conexão com a rede XMPP para ficar on-line e começar a receber eventos do socket.
3. `xmpp.sendMessage("destino@dominio.com", "forca_ilocutoria", "Mensagem a ser enviada")`: Metódo explicíto para que o Agente envie proativamente uma mensagem XMPP para outra entidade, convertendo isso para os padrões KQML.

## Como Rodar

### 1. Pré-Requisitos
- Visual Studio Code com a extensão [JaCaMO](https://marketplace.visualstudio.com/items?itemName=jason-lang.jacamo) instalada.
- Ter acesso a um Servidor XMPP (como o Openfire, Prosody ou Hotchilli) público ou instalado localmente.

### 2. Utilizando o XMPPAgent no seu projeto JaCaMO
A forma mais fácil de utilizar o `XMPPAgent` é indicar a dependência diretamente no arquivo `.jcm` do seu projeto JaCaMO, utilizando o link de *release* do JAR.

Adicione a arquitetura ao seu agente e informe o link de download no *class-path* do seu arquivo `.jcm`:

```jcm
mas meu_sistema_xmpp {
    
    agent meu_agente {
        ag-arch: jason.XMPPAgent
    }

    // Substitua pela versão do Release desejada do XMPPAgent
    use package: "https://github.com/chon-group/XMPPAgent/releases/download/v0.0.1/XMPPAgent-0.0.1-jar-with-dependencies.jar"
}
```

No código do agente em *AgentSpeak* (`meu_agente.asl`), inicialize a conexão através de seu *plan* inicial:
```jason
!start.

+!start : true <-
    .print("Inicializando Agente XMPP");
    xmpp.credentials("meu_agente@meuserver.com", "senha123");
    xmpp.chatService("127.0.0.1", 5222); /* Altere para o IP do Servidor XMPP */
    xmpp.sendMessage("outro_agente@meuserver.com", "tell", "Olá! Estou online!").
```

Sempre que a conta `meu_agente@meuserver.com` receber uma mensagem XMPP estruturada como `#tell valor_qualquer`, o Agente gerará a crença correspondente em sua base de crenças através do método *tell*.

---

## Exemplo: NCC-1701 (USS Enterprise)

A pasta [`Exemplo_ncc1701/`](Exemplo_ncc1701/) contém um Sistema Multiagente temático inspirado na série *Star Trek*, projetado para demonstrar as capacidades de comunicação inter-SMA do `XMPPAgent`. O nome da nave NCC-1701 (USS Enterprise) é utilizado como metáfora: assim como a nave se comunica com outras partes do universo, o SMA se comunica além de seus limites locais via XMPP.

### Agentes e Papéis

O sistema é composto por três agentes, cada um com um papel distinto:

| Agente | Papel | Arquitetura |
|--------|-------|-------------|
| `uhura` | Oficial de Comunicações — ponto central de comunicação do navio | `jason.XMPPAgent` |
| `scott` | Chefe de Engenharia — agente local que recebe alertas internos | Jason padrão |
| `kirk` | Capitão — opera em um **SMA externo e separado**, comunicando-se via XMPP | `jason.XMPPAgent` |

O agente **Kirk** não é declarado no arquivo `ncc1701.jcm` (que define o SMA da Enterprise). Ele representa um agente rodando em uma **instância JaCaMo diferente** (outro processo ou outra máquina), evidenciando comunicação verdadeiramente distribuída entre SMAs distintos.

### Fluxo de Interação

O cenário simula uma situação de emergência a bordo da nave:

```
[SMA Externo - Kirk]                    [SMA Enterprise - Uhura/Scott]
        |                                           |
        |  (1) tell communication(trying)  -------> |
        |       [via XMPP]                          | Uhura detecta chamada
        |                                           |
        | <------ (2) tell communication(ok)        |
        |              [via XMPP]                   |
        |                                           |
        |  (3) achieve damageReport  ------------> |
        |       [via XMPP]                          | Uhura: "Deck 2 compromised!"
        |                                           |
        | <------ (4) tell report("Deck 2")         |
        |              [via XMPP]                   |
        |                                           |
        |  (5) tell retransmit(scott,redAlert) ---> |
        |       [via XMPP]                          | Uhura encaminha internamente
        |                                           |
        |                                    [Jason] .send(scott, tell, redAlert)
        |                                           |
        |                                         Scott: "Entreprise, Red Alert!"
```

**Passo a passo:**
1. **Kirk** envia `communication(trying)` a **Uhura** via XMPP para testar o canal.
2. **Uhura** responde com `communication(ok)` via XMPP, confirmando a comunicação.
3. **Kirk** recebe a confirmação e envia `achieve damageReport` via XMPP, solicitando um relatório de danos.
4. **Uhura** processa o objetivo `!damageReport` e envia `tell report("Deck 2")` de volta a Kirk via XMPP.
5. **Kirk** recebe o relatório e envia `tell retransmit(scott, redAlert)` a Uhura via XMPP.
6. **Uhura** recebe a instrução de retransmissão e usa o `.send()` interno do Jason para entregar `redAlert` a **Scott** localmente.
7. **Scott** recebe a crença `redAlert` e aciona o alerta: *"Entreprise, Red Alert!"*.

### O que o Exemplo Demonstra

- **Comunicação inter-SMA via XMPP**: Kirk e Uhura estão em SMAs distintos e se comunicam transparentemente pelo protocolo XMPP.
- **Integração com comunicação intra-SMA**: Uhura age como *gateway*, recebendo mensagens externas (XMPP) e repassando-as internamente ao Scott com o mecanismo padrão do Jason (`.send()`).
- **Troca de performativas KQML**: O exemplo usa `tell` e `achieve` para transportar os atos de fala entre os agentes.
- **Servidor XMPP público**: As credenciais de Uhura apontam para `jabber.hot-chilli.net`, demonstrando uso de um servidor XMPP público e gratuito.

### Estrutura de Arquivos

```
Exemplo_ncc1701/
├── ncc1701.jcm          # Arquivo de configuração do SMA (Enterprise)
├── build.gradle         # Build Gradle com JaCaMo 1.3.0
└── src/
    └── agt/
        ├── uhura.asl    # Agente Uhura (comunicações, usa XMPPAgent)
        ├── scott.asl    # Agente Scott (engenharia, Jason padrão)
        └── kirk.asl     # Agente Kirk (externo, referência para outro SMA)
```

### Como Executar

**Pré-requisito:** Ter a extensão [JaCaMo](https://marketplace.visualstudio.com/items?itemName=jason-lang.jacamo) instalada no VS Code, ou o Gradle disponível no terminal.

1. Abra a pasta `Exemplo_ncc1701/` no VS Code.
2. Edite `uhura.asl` com as credenciais e servidor XMPP desejados.
3. Execute via VS Code ou pelo terminal:
   ```bash
   ./gradlew run
   ```
4. Para simular o Kirk em um SMA externo, crie um segundo projeto JaCaMo com o `kirk.asl` (adicionando as crenças iniciais `myCredentials`, `chat` e `uhuraContact`) e execute-o separadamente.