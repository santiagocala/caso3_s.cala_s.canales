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
	private static final int THREADS = 1;

	/**
	 * @param args
	 */
	

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		int ip = 6789;
		System.out.println(MAESTRO + "Puerto de conexion: " + ip);

		System.out.println("1. Servidor Seguro");
		System.out.println("2. Servidor Inseguro");

		String decision = br.readLine();


		System.out.println(MAESTRO + "Empezando servidor ...");
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);

		File resultados = null;
		String rutaResultados ="./resultados.txt";
		resultados = new File(rutaResultados);
		if (!resultados.exists()) {
			resultados.createNewFile();

		}
		FileWriter fwR = new FileWriter(resultados);
		fwR.close();
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

			D.init(certSer, keyPairServidor, fileSeguro,resultados);
		}
		else if (decision.equals("2")){
			File fileInseguro = null;
			String ruta = "./logInseguro.txt";
			fileInseguro = new File(ruta);
			if (!fileInseguro.exists()) {
				fileInseguro.createNewFile();

			}
			FileWriter fw = new FileWriter(fileInseguro);
			fw.close();

			DInseguro.init(certSer, keyPairServidor, fileInseguro,resultados);
		}

        
		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado");

		//pool de threads
		//int numeroDeNucleos = Runtime.getRuntime().availableProcessors();
		ExecutorService executer = Executors.newFixedThreadPool(THREADS);
        
		for (int i=0;true;i++) {
			try { 
				Socket sc = ss.accept();
				System.out.println(MAESTRO + "Cliente " + i + 1 + " aceptado.");

				if(decision.equals("1")){
					D d = new D(sc,i);
					executer.execute(d);
				}
				else if(decision.equals("2")){
					DInseguro di = new DInseguro(sc,i);
					executer.execute(di);
				}
				else{
					System.out.println("solamente se acepta 1 o 2 :)");
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
