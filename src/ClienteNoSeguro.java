import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteNoSeguro {
	
	private static Socket connection;
	private static PrintWriter pw;
    private static BufferedReader bf;
    private static InputStreamReader in;
	
	public static void main(String[] args){
		alistarConexion();
		pw.println("HOLA");
		
		
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
}
