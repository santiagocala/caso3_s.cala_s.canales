import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class testingg {
	private final static String padding = "AES/ECB/PKCS5Padding";//algoritmos y formato
    private static KeyGenerator keyGen;
    private static SecretKey KS;
    private static PrintWriter pw;
    private static BufferedReader bf;
    private static InputStreamReader in;
    
	public static void main(String args[]){
		
//		Cliente c = new Cliente();
//		c.execute();
		String s = "hola";
		System.out.println(s.hashCode());
		
	}
	private static String sumar0s(String m) {
		
		String sumado = m;
		for(int i = 0; i < (4 - (m.length() % 4)); i++){
			sumado = "0" + sumado;
		}
		return sumado;
		
	}
	
	private static byte[] cifrarSimetrico(SecretKey ks, String m){
        try {

            Cipher cifrador = Cipher.getInstance(padding);
            cifrador.init(Cipher.ENCRYPT_MODE,ks);

            return cifrador.doFinal(m.getBytes());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();return null;
        } catch (BadPaddingException e) {
            e.printStackTrace();return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();return null;
        }
    }

    private static byte[] descifrarSimetrico(byte[] texto, SecretKey ks){
    	byte[] textoClaro;
        try {
            Cipher descifrador = Cipher.getInstance(padding);
            descifrador.init(Cipher.DECRYPT_MODE,ks);
            textoClaro = descifrador.doFinal(texto);
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();return null;
        } catch (BadPaddingException e) {
            e.printStackTrace();return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();return null;
        }
        return textoClaro;
    }

    private static int imprimirBytes(byte[] bytes){
        String s = "";
        int i = 0;
        for(; i < bytes.length; i++){
            s += bytes[i];
        }
        System.out.println(s);
        return i;
    }

    //misma vaina que el simetrico pero mandando algoritmo al cifrador
    public static byte[] cifrarAsimetrico(Key pk, String algoritmo, String m){
        try {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            cifrador.init(Cipher.ENCRYPT_MODE, pk);

            return cifrador.doFinal(m.getBytes());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();return null;
        } catch (BadPaddingException e) {
            e.printStackTrace();return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();return null;
        }
    }

    //genera llaves con algoritmo AES
    private static SecretKey generateSimetricKey(){
		try {
			keyGen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return keyGen.generateKey();
	}
    private static String sumar16s(String prueba) {
    	String newString = prueba;
		while(newString.length() % 16 != 0){
			newString = "0" + newString;
		}
    	
		return newString;
	}
   

}
