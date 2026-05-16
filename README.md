# Java HSM Crypto Examples 🔐

[![Language: Java](https://img.shields.io/badge/Language-Java-ED8B00?style=flat-square&logo=java)](https://www.java.com/)
[![Security: HSM](https://img.shields.io/badge/Security-HSM_Integration-2B3467?style=flat-square)](https://www.dinamonetworks.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://opensource.org/licenses/MIT)

Este repositório contém exemplos de integração com **Hardware Security Modules (HSMs)** da Dinamo Networks utilizando Java. 

Demonstra operarações do HSM via código para realizar operações criptográficas, garantindo que o material criptográfico (chaves privadas e simétricas) nunca seja exposto na memória da aplicação, permanecendo seguro dentro do limite físico do hardware.

Este projeto foi desenvolvido como parte de atividades práticas na matéria de Segurança Computacional (UTFPR).

## 🚀 Implementações

### 1. Geração de Hash (`GenerateHash.java`)
Demonstra o cálculo de resumos criptográficos para o hardware de segurança.
* Leitura de entrada de dados dinâmica fornecida pelo usuário.
* Geração de hash utilizando o algoritmo **SHA-256** processado internamente pelo HSM.
* Conversão segura e formatação do array de bytes resultante para string **Hexadecimal**.

### 2. Cifragem Simétrica (`CifradorAESCBC.java`)
Demonstra a criação de chaves simétricas e cifragem de dados.
* Geração de chave **AES-128** nativa no HSM.
* Uso de IV (Initialization Vector) aleatório gerado com segurança criptográfica (`api.getRand(16)`).
* Cifragem em modo **CBC** com preenchimento **PKCS#5**.

### 3. Troca de Chaves por Curvas Elípticas (`EcdhKeyExchange.java`)
Simula um cenário de negociação de chaves entre Cliente e Servidor usando criptografia assimétrica.
* Criação de pares de chaves baseados em Curvas Elípticas (**SECP128R1**).
* Execução do algoritmo **ECDH X9.63** com hash **SHA-256** para derivar uma chave de sessão compartilhada e temporária de forma segura.

### 4. Protocolo de Transporte de Chaves (`KeyTransportProtocol.java`)
Implementa um fluxo completo de encapsulamento e transporte seguro de chaves de sessão.
* Geração de um segredo aleatório (32 bytes).
* Cifragem assimétrica do segredo utilizando **RSA-2048**.
* Função de Derivação de Chave (KDF) aplicando **SHA-256** para dividir o segredo em uma chave **AES-128** e um **IV** (16 bytes cada).
* Importação segura da chave simétrica para o HSM para processamento em lote.

## ⚙️ Pré-requisitos e Configuração

Para executar estes exemplos, você precisará de:
* Java Development Kit (JDK) 8 ou superior.
* Biblioteca Java da Dinamo (`TacNDJavaLib`).
* Acesso a um HSM Dinamo (físico ou simulador).

### 🔒 Variáveis de Ambiente (Importante)
Para garantir a segurança, as credenciais de acesso ao HSM **não estão no código-fonte**. Antes de executar qualquer classe, você deve configurar as seguintes variáveis de ambiente no seu sistema ou IDE:

```bash
# Exemplo de configuracao no terminal Linux/WSL
export HSM_IP="<endereco_ip_do_hsm>"
export HSM_USER="<seu_usuario>"
export HSM_PASS="<sua_senha>"
