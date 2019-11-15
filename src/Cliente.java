import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import uniandes.gload.core.Task;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class Cliente extends Task {

	//algoritmos y padding utilizados
    private final static String padding = "AES/ECB/PKCS5Padding";
    private final static String RSA = "RSA";
    private final static String AES = "AES";    
    private final static String HMAC = "HMACSHA256";
    private static int errores = 0;
    //deber�a ser static? yo lo quit�
    private  Socket connection;
    private  KeyGenerator keyGen;
    private  SecretKey KS;
    private  PrintWriter pw;
    private  BufferedReader bf;
    private  InputStreamReader in;
    private  PublicKey PK;



    public Cliente(){
    	    	
    }


    private static byte[] cifrarSimetrico(SecretKey ks, byte[] m){
        try {

            Cipher cifrador = Cipher.getInstance(padding);
            cifrador.init(Cipher.ENCRYPT_MODE,ks);

            return cifrador.doFinal(m);

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
        	System.out.println("por favor volver a correr el programa");
            e.printStackTrace();return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();return null;
        }
        return textoClaro;
    }

    private static void imprimirBytes(byte[] bytes){
        String s = "";
        for(int i = 0; i < bytes.length; i++){
            s += bytes[i];
        }
        System.out.println(s);
    }

    public static byte[] cifrarAsimetrico(Key pk, String algoritmo, byte[] m){
        try {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            cifrador.init(Cipher.ENCRYPT_MODE, pk);

            return cifrador.doFinal(m);

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
    public static byte[] descifrarAsimetrico(Key pk, String algoritmo, byte[] texto){
    	byte[] textoClaro;
    	try {
			Cipher descifrador = Cipher.getInstance(algoritmo);
			descifrador.init(Cipher.DECRYPT_MODE, pk);
			textoClaro = descifrador.doFinal(texto);
			return textoClaro;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return null;
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return null;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return null;
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return null;
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return null;
		}
    }

    //genera llaves con algoritmo AES
    private  void generateSimetricKey(){
        try {
            keyGen = KeyGenerator.getInstance(AES);
            KS = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    
	public byte[] hash(byte[] valor)
    {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(KS.getEncoded()));
        byte[] hbytes = new byte[hmac.getMacSize()];
        hmac.update(valor,0,valor.length);
        hmac.doFinal(hbytes, 0);
        
        return hbytes;
    }

    


	@Override
    public void execute(){
        /*
         * ETAPA 1: 
         */
    	//establecer conexi�n
        alistarConexion();
        pw.println("HOLA");
        
        //leer OK
        try {
            bf.readLine();
            

        } catch (IOException e) {
        	fail();
            e.printStackTrace();
        }

        /*
         * ETAPA 2
         */
        //mandar algoritmos en orden- simétrico, asimétrico y hmac
        String algoritmos = "ALGORITMOS:AES:RSA:HMACSHA256";
        pw.println(algoritmos);
        try {

            //leer OK
            bf.readLine();


            //leer certificado digital
            String CD = bf.readLine();

            try {

                //establecer estándar
                CertificateFactory f = CertificateFactory.getInstance("X.509");

                //extraer K+
                byte[] bytesCD = DatatypeConverter.parseBase64Binary(CD);
                InputStream input = new ByteArrayInputStream(bytesCD);
                X509Certificate certificate = (X509Certificate)f.generateCertificate(input);
                PK = certificate.getPublicKey();

                generateSimetricKey();
                

                //cifrar KS con la llave publica pk: cifrado asim�trico
                
//                String ksCifrada = new String(KS.getEncoded());
                byte[] bytesksCifrada = cifrarAsimetrico(PK,RSA,KS.getEncoded());


                

                //ETAPA 3
                //se envia la llave de sesión cifrada asimétricamente
                pw.println(DatatypeConverter.printBase64Binary(bytesksCifrada));

                //verificar que funciona el canal
                pw.println("reto");
                String prueba = bf.readLine();
                
                String reto = DatatypeConverter.printBase64Binary(descifrarSimetrico(sumar4s(prueba),KS));
                
                
                if(reto.equals("reto"))
                	pw.println("OK");
                else{
                	fail();
                	pw.println("ERROR");
                }	                	
                
                //ingreso de datos
                //Scanner in = new Scanner(System.in);
                //System.out.println("Ingrese su c�dula de ciudadan�a: ");
                //String cc = in.nextLine();
               // System.out.println("Ingrese su contrase�a: ");
                //String contrasena = in.nextLine();
                String cc = "8888";
                String contrasena = "1234";
                
                String CCcifrada = DatatypeConverter.printBase64Binary(cifrarSimetrico(KS, sumar4s(cc)));
                String contrasenaCifrada = DatatypeConverter.printBase64Binary(cifrarSimetrico(KS, sumar4s(contrasena)));
                
                //env�o de datos
                pw.println(CCcifrada);
                pw.println(contrasenaCifrada);
                
                
                //ETAPA 4
                //recibir valor y hmac para comparar 
                String valorCifradoKS = bf.readLine();
                String hmacCifradoPK = bf.readLine();
                
                
                byte[] valor = descifrarSimetrico(sumar4s(valorCifradoKS),KS);
                String hmac = DatatypeConverter.printBase64Binary(descifrarAsimetrico(PK, RSA, sumar4s(hmacCifradoPK)));
                
               	
				String hmacGeneradoPorValorRecibido =DatatypeConverter.printBase64Binary(hash(valor));
                	 
				if(hmacGeneradoPorValorRecibido.equals(hmac))pw.println("OK");
				else{
					fail();
					pw.println("ERROR");
				}
					

            } catch (CertificateException e) {
            	fail();
                e.printStackTrace();
            }

        } catch (IOException e) {
        	fail();
            e.printStackTrace();
        }

        //Cerrar
        pw.close();
        try {
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private static byte[] sumar4s(String rellenar) {
    	String newString = rellenar;
		while(newString.length() %  16 != 0){
			newString = "0" + newString;
		}
    	
		return DatatypeConverter.parseBase64Binary(newString);
	}


	private void alistarConexion() {
        try {

            connection = new Socket("157.253.201.154", 4561);
            pw = new PrintWriter(connection.getOutputStream(), true);
            in = new InputStreamReader(connection.getInputStream());
            bf = new BufferedReader(in);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	@Override
	public void fail() {
		errores++;
		System.out.println(Task.MENSAJE_FAIL);
	}


	@Override
	public void success() {
		// TODO Auto-generated method stub
		System.out.println(Task.OK_MESSAGE);
	}


	



}
