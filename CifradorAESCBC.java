import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.dinamonetworks.Dinamo;
import br.com.trueaccess.TacException;
import br.com.trueaccess.TacNDJavaLib;

public class CifradorAESCBC {
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

        System.out.println("--> Abrindo sessao no HSM...");
        api.openSession(hsmIp, hsmUser, hsmUserPassword);

        String plainText = "Mensagem confidencial para a Atividade 3";
        byte[] originalData = plainText.getBytes(StandardCharsets.UTF_8);

        // 1. Gerar IV aleatório de 16 bytes (tamanho do bloco do AES)
        byte[] iv = api.getRand(16);

        String keyId = "chave_aes_128_prova";

        // Garante que não tem lixo de execuções anteriores usando a função da sua API
        api.deleteKeyIfExists(keyId);

        System.out.println("--> Criando chave AES-128...");
        // Nota: Baseado no seu exemplo ALG_AES_256, usamos ALG_AES_128. 
        // Se a IDE não reconhecer, troque para TacNDJavaLib.ALG_AES
        api.createKey(keyId, TacNDJavaLib.ALG_AES_128);

        System.out.println("--> Cifrando dados (AES/CBC/PKCS5Padding)...");
        // 2. Cifrar os dados usando a assinatura EXATA da sua API
        byte[] encrypted = api.encrypt(
                keyId,
                originalData,
                iv,
                TacNDJavaLib.D_PKCS5_PADDING,
                TacNDJavaLib.MODE_CBC
        );

        // 3. Exibir os textos cifrados (e o IV) em Base64
        System.out.println("\n=== RESULTADOS ===");
        System.out.println("IV (Base64): " + Base64.getEncoder().encodeToString(iv));
        System.out.println("Texto Cifrado (Base64): " + Base64.getEncoder().encodeToString(encrypted));
        System.out.println("==================\n");

        System.out.println("--> Deletando chave e fechando sessao...");
        api.deleteKeyIfExists(keyId);
        api.closeSession();

        System.out.println("Processo finalizado com sucesso!");
    }
}