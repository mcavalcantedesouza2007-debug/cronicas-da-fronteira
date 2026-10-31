# CRÔNICAS DA FRONTEIRA RPG v0.3 — Minecraft Java 26.3 / Fabric

**Estado: código-fonte experimental, NÃO é um mod `.jar` compilado ou validado em jogo.**

Esta versão traz a campanha RPG de 12 capítulos e 18 contratos da v0.2 para a API do Minecraft **Java 26.3** e acrescenta uma **interface visual de conversação** com personagens, retratos pixel art originais e escolhas com consequências. Foi preparada para **Fabric**.

## Requisitos

- **Minecraft Java 26.3** com Fabric Loader instalado.
- **JDK 25**, não Java 21.
- **Gradle 9.7.1 ou superior**, acesso à internet para baixar dependências.
- **Fabric API 0.165.0+26.3** na pasta `mods` (ou versão compatível com 26.3).
- Projeto com **Fabric Loader 0.20.1** e **Fabric Loom 1.19-SNAPSHOT**.

Referências oficiais: https://fabricmc.net/develop/ e https://github.com/FabricMC/fabric-example-mod/tree/26.3

## Compilar

1. Instale JDK 25 e Gradle 9.7.1+; defina `JAVA_HOME` apontando para JDK 25.
2. Extraia este ZIP numa pasta. **Não coloque o ZIP dentro de `mods`.**
3. No Windows, execute `compilar-windows.bat`. No Linux, execute `./compilar-linux.sh`.
4. **Se a compilação terminar sem erros**, encontre o `.jar` em `build/libs/`, ignorando o arquivo `-sources.jar`.
5. Coloque esse `.jar` e o Fabric API no diretório `mods` de uma instalação com **Minecraft 26.3 + Fabric**.

Se ocorrerem erros de compilação, o projeto pode exigir ajustes de API; os testes deste pacote **não** garantem a compilação.

## Como jogar

- Entre no mundo e receba o **Diário do Aventureiro** na primeira conexão.
- **Clique direito** em um villager sem agachar: abre uma **janela de diálogo** com nome, retrato e respostas. Sua escolha pode alterar honra, afinidade, presentes e desbloqueios narrativos.
- Para trocar itens com um villager normalmente, **agache + clique direito**.
- **Agache + clique direito** em certas criaturas para analisá-las, oferecer itens e formar um vínculo (se o animal aceitar).
- Use `/cronicas` ou clique direito com o Diário para ver a campanha e o nível.
- Para missões, converse com o villager de profissão indicada e use a opção “Precisa de ajuda com alguma missão?”.

### Comandos

| Comando | Função |
| --- | --- |
| `/cronicas` ou `/cronicas diario` | Diário, capítulo, nível e progresso |
| `/cronicas honra` | Honra, reputação e escolhas lembradas |
| `/cronicas aceitar` | Aceita capítulo perto de villager da profissão necessária |
| `/cronicas entregar` | Entrega capítulo concluído |
| `/cronicas contratos` | Lista 18 missões secundárias |
| `/cronicas contrato <codigo>` | Aceita contrato |
| `/cronicas contrato entregar` | Entrega contrato completo |
| `/cronicas decidir libertar` ou `vender` | Decisão do capítulo 5 |
| `/cronicas decidir devolver` ou `roubar` | Decisão do capítulo 8 |
| `/cronicas decidir selar` ou `dominar` | Decisão do capítulo 12 |
| `/cronicas masmorra` | Informa quando uma masmorra está liberada |
| `/cronicas masmorra confirmar` | **Gera** cripta no terreno (destrutivo!) |
| `/cronicas entrar` e `/cronicas sair` | Teletransporte da cripta |
| `/cronicas diarioitem` | Recupera o diário |

## Recursos

- **30 missões**: 12 capítulos da campanha e 18 contratos repetíveis.
- **2 antagonistas**: Carcereiro Cinzento e Dama do Eclipse.
- **4 finais**, afetados por honra e decisões.
- **Honra persistente -100 a +100**, experiência RPG, níveis, esmeraldas e relações individuais guardadas pelo UUID do NPC.
- **6 perfis de diálogo**: fazendeiro, clérigo, bibliotecário, armeiro, ferreiro, viajante.
- **6 retratos pixel art originais** por profissão, perfis de personalidade e nomes determinados estavelmente pelo UUID do aldeão.
- **Árvores com respostas ramificadas**: ajuda, rumores, ameaças, doações, proteger moradores ou colaborar com o Eclipse; o servidor verifica distância, fase e elegibilidade antes de aplicar efeitos.
- **Mobs com interações específicas** para lobos, gatos, golems e hostis. São regras de comportamento, **não IA generativa** e não há conversação livre por texto.
- **Duas criptas** acessíveis em momentos específicos da história, com salas, monstros, chefes e tesouros.

## Avisos importantes

1. A cripta é criada pela modificação direta de blocos existentes em uma área **21 x 25 x 8** a cerca de 30 blocos de você; **faça backup e escolha um terreno dispensável**. Ela não aparece espontaneamente durante a exploração nesta versão.
2. Esta versão tem a interface de NPC **no cliente**, mas várias missões morais ainda usam o chat e comandos para confirmar ações; o menu fecha após ações que precisam de confirmação no servidor.
3. Compatibilidade pensada primeiro para **mundo single-player / servidor com o mod instalado nos dois lados**, não testada em ambiente multiplayer.
4. Para quem veio da v0.2: o sistema tenta manter o mesmo arquivo em `<mundo>/cronicasdafronteira/<UUID>.json`. **Faça backup do mundo e desses arquivos antes de atualizar.** Não execute a v0.2 de Minecraft 1.21.3 no 26.3.
5. A instalação não está garantida: o ambiente de desenvolvimento desta entrega possui apenas Java 21 e não tinha Gradle com as dependências do jogo; **não foi possível compilar um `.jar` ou testar no Minecraft 26.3**.

## Testes locais que podem rodar sem o Minecraft

Use `./testar-regras.sh` no Linux, ou compile os `tests/*.java` junto com `AdventureRules.java` e `DialogueData.java` usando `javac`. Esse script roda:

- `TestAdventureRules`: regras e integridade das 30 missões.
- `TestDialogueData`: opções, destinos, personagens e ramificações das árvores de diálogo.
- `ParseJavaSources`: análise sintática dos arquivos fonte usando a API do compilador Java.

**Não são testes de integração com Fabric.**

## Licença e ajustes

Os retratos fornecidos são desenhos originais do projeto. Ajuste a história nos arquivos `AdventureRules.java` e `DialogueData.java`. O código é fornecido para experimentação e evolução do mod.
