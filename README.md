# ProMapper

Projeto Android do ProMapper: perfis por jogo, comandos personalizados, sensibilidade, persistência local e telas de permissões/premium.

## O que já está implementado
- Interface gamer em Jetpack Compose.
- Perfis persistidos no aparelho.
- Criar, abrir, editar indiretamente e excluir perfis.
- Sensibilidade horizontal, vertical e mira.
- Criar/excluir comandos com tecla, ação e posição X/Y.
- Tela de permissões para sobreposição e acessibilidade.
- Serviço de acessibilidade base para integração futura com eventos de teclado.
- IDs definidos para planos: promapper_weekly, promapper_monthly, promapper_yearly.

## Importante
O Android limita a injeção de entrada em outros aplicativos. O serviço base não promete compatibilidade universal com jogos. Não há bypass de anti-cheat.

## Google Play
Cadastre os três produtos no Play Console e conecte a biblioteca Google Play Billing antes de publicar cobrança real. Os IDs usados pela interface são os três nomes acima.

## Build
Abra a pasta no Android Studio, sincronize o Gradle e gere um APK assinado. Este ambiente de conversa não possui Android SDK/Gradle configurados, portanto não é possível garantir um APK compilado aqui.
