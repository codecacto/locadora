# Configuracao do Firebase

Este projeto utiliza o Firebase Firestore como banco de dados. Siga os passos abaixo para configurar o Firebase.

## 1. Criar projeto no Firebase Console

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Clique em "Adicionar projeto"
3. Digite o nome do projeto: `locadora` (ou outro nome de sua preferencia)
4. Siga os passos para criar o projeto

## 2. Configurar Firebase para Android

### 2.1 Registrar o app Android

1. No Firebase Console, clique em "Adicionar app" e selecione Android
2. Digite o nome do pacote: `br.com.codecacto.locadora`
3. (Opcional) Digite um apelido para o app
4. (Opcional) Adicione o SHA-1 do certificado de depuracao
5. Clique em "Registrar app"

### 2.2 Baixar google-services.json

1. Baixe o arquivo `google-services.json`
2. Coloque o arquivo na pasta: `composeApp/`

```
locadora/
├── composeApp/
│   ├── google-services.json  <-- COLOQUE AQUI
│   ├── build.gradle.kts
│   └── src/
```

## 3. Configurar Firebase para iOS

### 3.1 Registrar o app iOS

1. No Firebase Console, clique em "Adicionar app" e selecione iOS
2. Digite o Bundle ID: `br.com.codecacto.locadora`
3. (Opcional) Digite um apelido para o app
4. Clique em "Registrar app"

### 3.2 Baixar GoogleService-Info.plist

1. Baixe o arquivo `GoogleService-Info.plist`
2. Coloque o arquivo na pasta: `iosApp/iosApp/`

```
locadora/
├── iosApp/
│   ├── iosApp/
│   │   ├── GoogleService-Info.plist  <-- COLOQUE AQUI
│   │   └── Info.plist
│   └── iosApp.xcodeproj/
```

## 4. Configurar Firestore

### 4.1 Habilitar Firestore

1. No Firebase Console, va em "Build" > "Firestore Database"
2. Clique em "Criar banco de dados"
3. Selecione o modo de inicio (recomendado: modo de teste para desenvolvimento)
4. Selecione a regiao do servidor

### 4.2 Regras de Seguranca (Desenvolvimento)

Para desenvolvimento, use as seguintes regras:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### 4.3 Regras de Seguranca (Producao)

Para producao, use regras mais restritivas:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Clientes - apenas usuarios autenticados
    match /clientes/{clienteId} {
      allow read, write: if request.auth != null;
    }

    // Equipamentos - apenas usuarios autenticados
    match /equipamentos/{equipamentoId} {
      allow read, write: if request.auth != null;
    }

    // Locacoes - apenas usuarios autenticados
    match /locacoes/{locacaoId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 5. Estrutura das Colecoes

O app utiliza as seguintes colecoes no Firestore:

### 5.1 Colecao `clientes`

```json
{
  "id": "string (auto-gerado)",
  "nomeRazao": "string",
  "cpfCnpj": "string | null",
  "telefoneWhatsapp": "string",
  "email": "string | null",
  "endereco": "string | null",
  "precisaNotaFiscalPadrao": "boolean",
  "criadoEm": "number (timestamp)",
  "atualizadoEm": "number (timestamp)"
}
```

### 5.2 Colecao `equipamentos`

```json
{
  "id": "string (auto-gerado)",
  "nome": "string",
  "categoria": "string",
  "identificacao": "string | null",
  "valorCompra": "number | null",
  "precoPadraoLocacao": "number",
  "observacoes": "string | null",
  "criadoEm": "number (timestamp)",
  "atualizadoEm": "number (timestamp)"
}
```

### 5.3 Colecao `locacoes`

```json
{
  "id": "string (auto-gerado)",
  "clienteId": "string",
  "equipamentoId": "string",
  "valorLocacao": "number",
  "dataInicio": "number (timestamp)",
  "dataFimPrevista": "number (timestamp)",
  "statusEntrega": "NAO_AGENDADA | AGENDADA | ENTREGUE",
  "dataEntregaPrevista": "number (timestamp) | null",
  "dataEntregaReal": "number (timestamp) | null",
  "statusPagamento": "PENDENTE | PAGO",
  "dataPagamento": "number (timestamp) | null",
  "statusColeta": "NAO_COLETADO | COLETADO",
  "dataColeta": "number (timestamp) | null",
  "emitirNota": "boolean",
  "notaEmitida": "boolean",
  "statusLocacao": "ATIVA | FINALIZADA",
  "qtdRenovacoes": "number",
  "ultimaRenovacaoEm": "number (timestamp) | null",
  "criadoEm": "number (timestamp)",
  "atualizadoEm": "number (timestamp)"
}
```

## 6. Configurar Crashlytics

### 6.1 Habilitar Crashlytics no Firebase Console

1. No Firebase Console, va em "Build" > "Crashlytics"
2. Clique em "Habilitar Crashlytics"
3. Siga os passos para configurar

### 6.2 Android

O Crashlytics ja esta configurado automaticamente pelo plugin no `build.gradle.kts`.

### 6.3 iOS

1. No terminal, navegue ate a pasta `iosApp`:
   ```bash
   cd iosApp
   pod install
   ```

2. Abra o projeto usando o arquivo `.xcworkspace` (NAO o `.xcodeproj`):
   ```bash
   open iosApp.xcworkspace
   ```

3. No Xcode, adicione um Build Phase para upload de dSYMs:
   - Selecione o target `iosApp`
   - Va em "Build Phases"
   - Clique em "+" e selecione "New Run Script Phase"
   - Adicione o script:
   ```bash
   "${PODS_ROOT}/FirebaseCrashlytics/run"
   ```
   - Marque "Run script only when installing"

### 6.4 Testar Crashlytics

Para testar se o Crashlytics esta funcionando, voce pode forcar um crash:

```kotlin
// Em algum lugar do app (apenas para teste!)
throw RuntimeException("Teste de Crashlytics")
```

Apos o crash, reinicie o app e verifique o Firebase Console > Crashlytics.

## 7. Indices (Opcionais)

Para melhor performance em consultas, crie os seguintes indices:

1. **Locacoes ativas por cliente**
   - Colecao: `locacoes`
   - Campos: `clienteId (ASC)`, `statusLocacao (ASC)`

2. **Locacoes por equipamento**
   - Colecao: `locacoes`
   - Campos: `equipamentoId (ASC)`, `statusLocacao (ASC)`

3. **Locacoes por data**
   - Colecao: `locacoes`
   - Campos: `statusLocacao (ASC)`, `dataFimPrevista (ASC)`

## 8. Verificar Configuracao

Apos configurar, execute o app e verifique:

1. O app deve iniciar sem erros relacionados ao Firebase
2. Ao criar um cliente, ele deve aparecer no Firestore Console
3. Ao criar um equipamento, ele deve aparecer no Firestore Console
4. Ao criar uma locacao, ela deve aparecer no Firestore Console

## Troubleshooting

### Erro: "Default FirebaseApp is not initialized"

- Verifique se o arquivo `google-services.json` esta na pasta correta
- Verifique se o plugin do Google Services esta aplicado no `build.gradle.kts`

### Erro: "PERMISSION_DENIED"

- Verifique as regras de seguranca do Firestore
- Verifique se o usuario esta autenticado (se necessario)

### Erro: "Could not reach Cloud Firestore backend"

- Verifique a conexao com a internet
- Verifique se o Firestore esta habilitado no projeto

## Recursos Adicionais

- [Documentacao do Firebase](https://firebase.google.com/docs)
- [GitLive Firebase SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- [Firestore Android](https://firebase.google.com/docs/firestore/quickstart)
