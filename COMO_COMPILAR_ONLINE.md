# Compilar o mod online — sem instalar Java ou Gradle

Este ZIP contém **código-fonte experimental** e uma automação GitHub Actions. **Não contém um JAR testado**.

## Passo a passo (navegador)

1. Acesse https://github.com/new, crie uma conta se necessário e crie um repositório **vazio**, de preferência `cronicas-da-fronteira` (não marque README, .gitignore nem licença para começar vazio).
2. Extraia este ZIP em uma pasta do computador.
3. Na página do novo repositório, clique em **uploading an existing file** (ou **Add file > Upload files**). Arraste o **conteúdo** da pasta extraída — `build.gradle`, `src`, `settings.gradle`, `.github` e outros arquivos — para a área de upload. Não envie somente o ZIP. Mantenha o arquivo `.github/workflows/compilar.yml` na pasta correspondente; pastas com ponto podem ficar ocultas no Windows. Se o navegador não aceitar arrastar a pasta `.github`, crie o arquivo pelo site: **Add file > Create new file** e nomeie `.github/workflows/compilar.yml`, colando o YAML fornecido neste pacote.
4. Clique em **Commit changes**.
5. Abra a aba **Actions** e selecione **Compilar Crônicas da Fronteira (Minecraft 26.3)**. O workflow inicia automaticamente após o envio ou pelo botão **Run workflow**.
6. Se a compilação finalizar com um ícone verde, abra sua execução e, em **Artifacts**, clique em **Cronicas-da-Fronteira-26.3-JAR**. O navegador baixará um ZIP que contém o `.jar` do mod.
7. Extraia o `.jar` e coloque-o em `.minecraft/mods` junto da versão adequada do Fabric API e execute o Minecraft com o Fabric Loader para 26.3. Para **jogar**, é necessário ter o Minecraft e o Fabric instalados; a etapa dispensada é instalar ferramentas de desenvolvimento.

## Se aparecer erro em vermelho

- Abra o item **Compilar mod** na execução e copie as primeiras mensagens com `error:` ou `FAILURE: Build failed` (e as linhas logo acima).
- Envie aqui essas mensagens para correção. O projeto anterior ainda não foi compilado contra todas as dependências do Minecraft 26.3: a automação pode descobrir erros reais na API ou no código.

## Notas

- GitHub Actions pode exigir a habilitação de Actions no repositório.
- O build pode demorar enquanto baixa Minecraft/Fabric/Gradle. A conta gratuita está sujeita aos limites de uso do GitHub.
- A automação usa Java 25, Gradle 9.7.1 e `gradle build`.
- O projeto não inclui um servidor ou mundo Minecraft para testar jogabilidade dentro do GitHub.
