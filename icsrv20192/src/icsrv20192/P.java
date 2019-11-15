package icsrv20192;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class P {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */

	/**
	 * @param args
	 */
	

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		int ip = Integer.parseInt(br.readLine());

		System.out.println("1. Servidor Seguro");
		System.out.println("2. Servidor Inseguro");

		String decision = br.readLine();


		System.out.println(MAESTRO + "Empezando servidor ...");
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);

		// Crea el archivo de log
		if(decision.equals("1")){
			File fileSeguro = null;
			String ruta = "./logSeguro.txt";
			fileSeguro = new File(ruta);
			if (!fileSeguro.exists()) {
				fileSeguro.createNewFile();

			}
			FileWriter fw = new FileWriter(fileSeguro);
			fw.close();

			D.init(certSer, keyPairServidor, fileSeguro);
		}
		else if (decision.equals("2")){
			File fileInseguro = null;
			String ruta = "./logSeguro.txt";
			fileInseguro = new File(ruta);
			if (!fileInseguro.exists()) {
				fileInseguro.createNewFile();

			}
			FileWriter fw = new FileWriter(fileInseguro);
			fw.close();

			DInseguro.init(certSer, keyPairServidor, fileInseguro);
		}

        
		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");

		//pool de threads
		int numeroDeNucleos = Runtime.getRuntime().availableProcessors();
		ExecutorService executer = Executors.newFixedThreadPool(numeroDeNucleos);
        
		for (int i=0;true;i++) {
			try { 
				Socket sc = ss.accept();
				System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
				//pool.execute(new D(sc,i));
				if(decision.equals("1")){
					D d = new D(sc,i);
					executer.execute(d);
				}
				else if(decision.equals("2")){
					DInseguro di = new DInseguro(sc,i);
					executer.execute(di);
				}
				else{
					System.out.println("no sea terco, es 1 o 2");
					return;
				}

				//ss.setSoTimeout(10000000);//termina la transacción si el cliente se demora mucho


			} catch (IOException e) {
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				e.printStackTrace();
			}

		}


	}
}
