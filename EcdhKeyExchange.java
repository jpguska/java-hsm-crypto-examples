import com.dinamonetworks.Dinamo;
import br.com.trueaccess.TacException;
import br.com.trueaccess.TacNDJavaLib;
import java.util.Base64;

public class EcdhKeyExchange {
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

            String clienteKey = "cliente_ecc_priv";
            String servidorKey = "servidor_ecc_par";
            String sessaoX963Key = "chave_sessao_x963";

            api.deleteKeyIfExists(clienteKey);
            api.deleteKeyIfExists(servidorKey);
            api.deleteKeyIfExists(sessaoX963Key);

            System.out.println("--> 1. Gerando chave do Cliente (Simulando importacao)...");
            api.createKey(clienteKey, TacNDJavaLib.ALG_ECC_SECP128R1);

            System.out.println("--> 2. Criando par de chaves do Servidor...");
            api.createKey(servidorKey, TacNDJavaLib.ALG_ECC_SECP128R1);

            System.out.println("--> 3. Exportando chave Publica do Servidor...");
            byte[] pubKeyServidor = api.exportKey(servidorKey, TacNDJavaLib.PUBLICKEY_BLOB);
            System.out.println("Public Key (Base64): " + Base64.getEncoder().encodeToString(pubKeyServidor));

            // ====================================================================
            // ACORDO DE CHAVES (Apenas ECDH X963 - O que funcionou no seu HSM)
            // ====================================================================
            
            byte[] kdfData = api.getRand(16);

            System.out.println("\n--> Executando ECDH Padrao X963...");
            api.genEcdhKeyX963Sha256(
                clienteKey,                  
                sessaoX963Key,               
                TacNDJavaLib.ALG_AES_128,    
                true,                        
                false,                       
                pubKeyServidor,              
                kdfData                      
            );
            System.out.println("Sucesso! Chave X963 armazenada no HSM sob o nome: " + sessaoX963Key);

            System.out.println("\n--> Deletando chaves e fechando sessao...");
            api.deleteKeyIfExists(clienteKey);
            api.deleteKeyIfExists(servidorKey);
            api.deleteKeyIfExists(sessaoX963Key);
            api.closeSession();

            System.out.println("Processo finalizado com sucesso!");

        } catch (TacException e) {
            System.err.println("Erro na API do Dinamo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}