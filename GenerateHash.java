import com.dinamonetworks.Dinamo;
import br.com.trueaccess.TacException;
import br.com.trueaccess.TacNDJavaLib;
import java.util.HexFormat;
import java.util.Scanner;

public class GenerateHash {
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
        Scanner scanner = new Scanner(System.in);
        api.openSession(hsmIp, hsmUser, hsmUserPassword, false);


        String M = scanner.nextLine();
        byte[] message = M.getBytes();


        byte[] hash = api.generateHash(TacNDJavaLib.ALG_SHA2_256, message);
        String hex = HexFormat.of().formatHex(hash);
        System.out.println(hex);
        api.closeSession();
    }
}