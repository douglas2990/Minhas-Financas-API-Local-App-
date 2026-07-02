# MinhasFinancas2990 💰

O **MinhasFinancas2990** é um aplicativo Android moderno focado em gestão financeira pessoal, desenvolvido para oferecer uma experiência intuitiva e eficiente no controle de gastos e receitas.

## 🚀 Tecnologias Utilizadas
Este projeto utiliza as práticas mais modernas de desenvolvimento Android (Modern Android Development - MAD):

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose (UI Declarativa)
- **Arquitetura:** MVVM (Model-View-ViewModel)
- **Injeção de Dependência:** Dagger Hilt
- **Networking:** Retrofit + Gson
- **Banco de Dados:** Room Persistence Library
- **Assincronismo:** Coroutines & Flow

## 📋 Funcionalidades
- [ ] Cadastro e gestão de transações financeiras.
- [ ] Visualização de saldo e relatórios básicos.
- [ ] Persistência local com Room para uso offline.
- [ ] Integração com API externa para sincronização de dados.

## ⚙️ Como configurar o projeto

Para compilar o projeto corretamente, você precisará configurar a chave da sua API no arquivo de propriedades local.

1. Clone o repositório:
   `git clone git clone https://github.com/douglas2990/Minhas-Financas-API-Local-App-.git`

2. Na pasta raiz do projeto, crie um arquivo chamado `local.properties`.

3. Adicione a URL da sua API no arquivo:
   ```properties
   API_URL=[localhost da sua API])


## 🏗️ Estrutura do Projeto

O projeto segue os princípios de **Clean Architecture**, garantindo uma separação clara de responsabilidades:

* **`data/`**: Camada de dados responsável pela comunicação com fontes externas (API/Retrofit) e locais (Room Database), incluindo Repositórios e modelos de dados.
* **`di/`**: Módulos do **Dagger Hilt** configurados para a injeção de dependência, garantindo um código desacoplado.
* **`ui/`**: Camada de interface contendo as `Composable functions` (UI) e os `ViewModels` responsáveis pelo gerenciamento de estado e lógica de tela.

## 🛠️ Contribuições

Este projeto está em desenvolvimento ativo. Sinta-se à vontade para sugerir melhorias, abrir *issues* ou enviar *pull requests*. Toda contribuição, seja corrigindo um bug ou sugerindo uma nova funcionalidade, é muito bem-vinda!

---
*Desenvolvido com dedicação por [douglas2990](https://github.com/douglas2990).*
