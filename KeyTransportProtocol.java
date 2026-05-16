import com.dinamonetworks.Dinamo;
import br.com.trueaccess.TacException;
import br.com.trueaccess.TacNDJavaLib;

import java.util.Base64;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public class KeyTransportProtocol {
    // Credenciais
    static String hsmIp = System.getenv("HSM_IP");
    static String hsmUser = System.getenv("HSM_USER");
    static String hsmUserPassword = System.getenv("HSM_PASS");
    public static void main(String[] args) throws TacException {
        if (hsmIp == null || hsmUser == null || hsmUserPassword == null) {
            System.err.println("ERRO: Configure as variáveis de ambiente (HSM_IP, HSM_USER, HSM_PASS) antes de rodar.");
            return;
        }

        Dinamo api = new Dinamo();

        try {
            System.out.println("--> Abrindo sessao no HSM...");
            api.openSession(hsmIp, hsmUser, hsmUserPassword, false);

            String rsaKeyName = "rsa_transport_key";
            api.deleteKeyIfExists(rsaKeyName);

            System.out.println("--> Passo 2: Criando par de chaves RSA (PK, SK) do Servidor...");
            api.createKey(rsaKeyName, TacNDJavaLib.ALG_RSA_2048);

            System.out.println("\n=== INICIANDO LOOP DE 7 CHAVES ===");

            // Passo 4: Repita os passos de 1 a 3 (7 vezes)
            for (int i = 1; i <= 7; i++) {
                System.out.println("\n--- ITERACAO " + i + " ---");

                // Passo 1: Gere valor aleatorio de 32 bytes (Segredo K)
                byte[] K = api.getRand(32);

                // Passo 2: Cifre K com a chave publica RSA (PK)
                // CORREÇÃO: Usando o método simples, o HSM detecta que é RSA automaticamente!
                byte[] C = api.encrypt(rsaKeyName, K);

                // Passo 3: Exiba C em tela, codificado em base64
                System.out.println("Texto Cifrado RSA 'C' (Base64): " + Base64.getEncoder().encodeToString(C));

                // Passo 5: Verifique se decifrando C com a chave privada (SK) obtemos K
                // CORREÇÃO: Usando o método simples
                byte[] K_decifrado = api.decrypt(rsaKeyName, C);

                if (Arrays.equals(K, K_decifrado)) {
                    System.out.println("[OK] Verificacao RSA: Segredo 'K' recuperado com sucesso!");
                } else {
                    System.out.println("[ERRO] Verificacao RSA falhou!");
                    continue; 
                }

                // Passo 6: Utilize SHA-256 em K como KDF
                byte[] hashKS = api.generateHash(TacNDJavaLib.ALG_SHA2_256, K);

                // Passo 7: 16 bytes para Chave AES, 16 bytes para IV
                byte[] aesKeyBytes = Arrays.copyOfRange(hashKS, 0, 16);
                byte[] ivBytes = Arrays.copyOfRange(hashKS, 16, 32);

                String aesKeyName = "aes_transport_chave"; 
                api.deleteKeyIfExists(aesKeyName);

                // Importando a chave AES pro HSM
                api.importKey(
                    aesKeyName, 
                    TacNDJavaLib.PLAINTEXTKEY_BLOB, 
                    TacNDJavaLib.ALG_AES_128, 
                    TacNDJavaLib.NONEXPORTABLE_KEY, 
                    aesKeyBytes, 
                    TacNDJavaLib.ALG_AES_128_LEN
                );

                // Passo 8: Cifre 7 mensagens de escolha
                String mensagem = "Mensagem ultra secreta numero " + i + " do protocolo.";
                byte[] dados = mensagem.getBytes(StandardCharsets.UTF_8);

                // Aqui para o AES mantemos o método completo, que sabemos que funciona!
                byte[] mensagemCifrada = api.encrypt(
                    aesKeyName, 
                    dados, 
                    ivBytes, 
                    TacNDJavaLib.D_PKCS5_PADDING, 
                    TacNDJavaLib.MODE_CBC
                );

                System.out.println("Texto Cifrado AES (Base64): " + Base64.getEncoder().encodeToString(mensagemCifrada));

                // Limpeza da chave AES da iteracao atual
                api.deleteKeyIfExists(aesKeyName);
            }

            System.out.println("\n--> Limpeza final e fechando sessao...");
            api.deleteKeyIfExists(rsaKeyName);
            api.closeSession();
            System.out.println("Processo finalizado com sucesso! Voce esta pronto para a prova!");

        } catch (TacException e) {
            System.err.println("Erro na API do Dinamo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}