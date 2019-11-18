import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import uniandes.gload.core.Task;


public class ClienteNoSeguro extends Task{

	
	private static String ALGORITMOS = "ALGORITMOS";
	private static String ASIMETRICO = "RSA";
	private static String SIMETRICO = "AES";
	private static String HMAC = "HMACSHA256";
	
	@Override
	public void execute(){
		// Ver protocolo para entender la transferencia de mensajes. 
		
		Socket connection;
		PrintWriter pw;
		BufferedReader bf;
		InputStreamReader in;
		KeyGenerator keyGen;
		SecretKey sk;

		
		try {
			connection = new Socket("localhost", 6789);
			pw = new PrintWriter(connection.getOutputStream(), true);
			in = new InputStreamReader(connection.getInputStream());
			bf = new BufferedReader(in);
			
			pw.println("HOLA");
			try{
				bf.readLine();
			} catch(Exception e) {
				fail();
				e.printStackTrace();
			}
			
			// Se envían los algortimos de cifrado, simétrico, asimétrico y MAC 
			pw.println(ALGORITMOS +  ":" + SIMETRICO + ":" + ASIMETRICO + ":" + HMAC);
			String mensaje = "";
			try{
				mensaje = bf.readLine();
			} catch(Exception e) {
				e.printStackTrace();
			}
			// Verifica que el servidor haya respondido "OK" a los algoritmos que se le enviaron. 
			if(mensaje == "ERROR"){
				fail();
				System.out.println("Se produjo un error después de mandar los algoritmos");
			}
			// Recibe el certificado digital
			try{
				// Acá se recibe el certificado, pero en realidad no se hace nada con él
				bf.readLine();
				// Se genera una llave simétrica y se manda por el canal y después el reto. 
				try {
					keyGen = KeyGenerator.getInstance(SIMETRICO);
					sk = keyGen.generateKey();
					pw.println(new String(sk.getEncoded()) ); 
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				
				//el reto
				pw.println("santiycan");
			} catch(Exception e){
				fail();
				e.printStackTrace();
			}
			//Variable que tomará el valor del mensaje a mandar.
			//Depende de si el mensaje recibido es el correcto o no. 
			String respuesta = "";
			try{
				mensaje = bf.readLine();
			} catch(Exception e) {
				e.printStackTrace();
			}
			// Verifica que el servidor haya respondido el mismo reto que enviaron. 
			if(mensaje.equals("santiycan")){
				respuesta = "OK";
			}
			else{
				fail();
				respuesta = "ERROR";
			}
			pw.println(respuesta);
			
			//Se envían la cédula y la contraseña por el canal de comunicación
			pw.println("1234");
			pw.println("1234");
			String valorCorrecto = "";
			try{
				//Se recibe el valor y con ese valor se calcula el hash
				String valor = bf.readLine();
				String hashValorLocal = valor.hashCode() + "";
				//Se recibe el hash del valor que se mandó 
				String hashValorExterno = bf.readLine();
				//Se verifica que los hashes sean iguales 
				if(hashValorLocal.contentEquals(hashValorExterno)) {
					valorCorrecto = "OK";
				}
				else {
					fail();
					valorCorrecto = "ERROR";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Se envía el mensaje que indica si el valor es correcto o no. 
			pw.println(valorCorrecto);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}



	/**
	 * Método que se encarga de generar una llave simétrica secreta
	 */
	
	public String toHexString(byte[] array) {
        return DatatypeConverter.printBase64Binary(array);
    }
	public byte[] toByteArray(String s) {
        return DatatypeConverter.parseBase64Binary(s);
    }

	@Override
	public void fail() {
		
		System.out.println(Task.MENSAJE_FAIL);		
		
	}

	@Override
	public void success() {
		System.out.println(Task.OK_MESSAGE);
	}
	

	

	
}
