import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class ClienteNoSeguro {

	private static Socket connection;
	private static PrintWriter pw;
	private static BufferedReader bf;
	private static InputStreamReader in;
	private static String ALGORITMOS = "ALGORITMOS";
	private static String ASIMETRICO = "RSA";
	private static String SIMETRICO = "AES";
	private static String HMAC = "HMACSHA256";
	private static KeyGenerator keyGen;


	public static void main(String[] args){
		// Ver protocolo para entender la transferencia de mensajes. 
		alistarConexion();
		pw.println("HOLA");
		try{
			bf.readLine();
		} catch(Exception e) {
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
			System.out.println("Se produjo un error después de mandar los algoritmos");
		}
		// Recibe el certificado digital
		try{
			// Acá se recibe el certificado, pero en realidad no se hace nada con él
			bf.readLine();
			// Se genera una llave simétrica y se manda por el canal y después el reto. 
			SecretKey sk = generateSimetricKey();
			pw.println(sk);
			pw.println("reto");
		} catch(Exception e){
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
		if(mensaje != "reto"){
			respuesta = "ERROR";
			System.out.println("Se produjo un error después de leer el reto");
		}
		else{
			respuesta = "OK";
		}
		pw.println(respuesta);
		// Se abre un canal por donde el usuario puede ingresar su cédula y contraseña
		Scanner scanner = new Scanner(System.in);
		System.out.println("Ingrese su cédula: ");
		String cedula = scanner.nextLine();
		System.out.println("Ingrese su contraseña: ");
		String contrasena = scanner.nextLine();
		//Se envían la cédula y la contraseña por el canal de comunicación
		pw.println(cedula);
		pw.println(contrasena);
		scanner.close();
		String valorCorrecto = "";
		try{
			//Se recibe el valor y con ese valor se calcula el hash
			String valor = bf.readLine();
			int hashValorLocal = valor.hashCode();
			//Se recibe el hash del valor que se mandó 
			int hashValorExterno = Integer.parseInt(bf.readLine());
			//Se verifica que los hashes sean iguales 
			if(hashValorLocal == hashValorExterno) valorCorrecto = "OK";
			else valorCorrecto = "ERROR";
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Se envía el mensaje que indica si el valor es correcto o no. 
		pw.println(valorCorrecto);
	}

	/**
	 * Método que se encarga de alistar la conexión que va a tener el cliente con el servidor
	 */
	private static void alistarConexion() {
		// Se crea la conexión y los elementos que servirán para la comunicación. 
		try {
			connection = new Socket("localhost", 6667);
			pw = new PrintWriter(connection.getOutputStream(), true);
			in = new InputStreamReader(connection.getInputStream());
			bf = new BufferedReader(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método que se encarga de generar una llave simétrica secreta
	 */
	private static SecretKey generateSimetricKey(){
		try {
			keyGen = KeyGenerator.getInstance(SIMETRICO);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return keyGen.generateKey();
	}
}
