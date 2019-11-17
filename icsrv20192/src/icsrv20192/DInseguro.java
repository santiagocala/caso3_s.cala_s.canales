package icsrv20192;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Random;

public class DInseguro implements Runnable {
    public static final String OK = "OK";
    public static final String ALGORITMOS = "ALGORITMOS";
    public static final String CERTSRV = "CERTSRV";
    public static final String CERCLNT = "CERCLNT";
    public static final String SEPARADOR = ":";
    public static final String HOLA = "HOLA";
    public static final String INICIO = "INICIO";
    public static final String ERROR = "ERROR";
    public static final String REC = "recibio-";
    public static final int numCadenas = 8;
    private static int exitosos = 0;

    // Atributos
    private Socket sc = null;
    private String dlg;
    private byte[] mybyte;
    private static File file;
    private static X509Certificate certSer;
    private static KeyPair keyPairServidor;
    
    private final static int TOTAL_TRANS = 400;
   
    private static int errores = 0;
	private static double acumuladoCPU = 0;
	private static double acumuladoTiempoTransaccion = 0;

    public static void init(X509Certificate pCertSer, KeyPair pKeyPairServidor, File pFile) {
        certSer = pCertSer;
        keyPairServidor = pKeyPairServidor;
        file = pFile;
    }

    public DInseguro (Socket csP, int idP) {
        sc = csP;
        dlg = new String("delegado " + idP + ": ");
        try {
            mybyte = new byte[520];
            mybyte = certSer.getEncoded();
        } catch (Exception e) {
            System.out.println("Error creando encoded del certificado para el thread" + dlg);
            e.printStackTrace();
        }
    }

    private boolean validoAlgHMAC(String nombre) {
        return ((nombre.equals(S.HMACMD5) ||
                nombre.equals(S.HMACSHA1) ||
                nombre.equals(S.HMACSHA256) ||
                nombre.equals(S.HMACSHA384) ||
                nombre.equals(S.HMACSHA512)
        ));
    }

    /*
     * Generacion del archivo log.
     * Nota:
     * - Debe conservar el metodo como está.
     * - Es el único metodo permitido para escribir en el log.
     */
    private void escribirMensaje(String pCadena) {

        try {
            FileWriter fw = new FileWriter(file,true);
            fw.write(pCadena + "\n");
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        String[] cadenas;
        cadenas = new String[numCadenas];

        String linea;
        System.out.println(dlg + "Empezando atencion.");
        try {

            PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
            BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

            /***** Fase 1:  *****/
            linea = dc.readLine();
            cadenas[0] = "Fase1: ";
            if (!linea.equals(HOLA)) {
                ac.println(ERROR);
                sc.close();
                errores();
                throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
            } else {
                ac.println(OK);
                cadenas[0] = dlg + REC + linea + "-continuando.";
                System.out.println(cadenas[0]);

            }

            /***** Fase 2:  *****/
            linea = dc.readLine();
            cadenas[1] = "Fase2: ";
            if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(ALGORITMOS))) {
                ac.println(ERROR);
                errores();
                sc.close();
                throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
            }

            String[] algoritmos = linea.split(SEPARADOR);
            if (!algoritmos[1].equals(S.DES) && !algoritmos[1].equals(S.AES) &&
                    !algoritmos[1].equals(S.BLOWFISH) && !algoritmos[1].equals(S.RC4)){
                ac.println(ERROR);
                errores();
                sc.close();
                throw new Exception(dlg + ERROR + "Alg.Simetrico" + REC + algoritmos + "-terminando.");
            }
            if (!algoritmos[2].equals(S.RSA) ) {
                ac.println(ERROR);
                errores();
                sc.close();
                throw new Exception(dlg + ERROR + "Alg.Asimetrico." + REC + algoritmos + "-terminando.");
            }
            if (!validoAlgHMAC(algoritmos[3])) {
                ac.println(ERROR);
                errores();
                sc.close();
                throw new Exception(dlg + ERROR + "AlgHash." + REC + algoritmos + "-terminando.");
            }
            cadenas[1] = dlg + REC + linea + "-continuando.";
            System.out.println(cadenas[1]);
            ac.println(OK);

            /***** Fase 3:  *****/
            String testCert = toHexString(mybyte); //aca hubo cambios
            System.out.println(testCert);
            ac.println(testCert);
            cadenas[2] = dlg + "envio certificado del servidor. continuando.";
            System.out.println(cadenas[2] + testCert);

            /***** Fase 4: *****/
            cadenas[3] = "";
            linea = dc.readLine();
            registrarCpu();
            long comienzoTransaccion = System.currentTimeMillis();
            cadenas[3] = dlg + "recibio. continuando.";
            System.out.println(cadenas[3]);

            /***** Fase 5:  *****/
            cadenas[4]="";
            linea = dc.readLine();
            System.out.println(dlg + "Recibio reto del cliente:-" + linea + "-");
            ac.println(linea);
            registrarCpu();
            System.out.println(dlg + "envio reto . continuado.");

            linea = dc.readLine();
            if ((linea.equals(OK))) {
                cadenas[4] = dlg + "recibio confirmacion del cliente:"+ linea +"-continuado.";
                System.out.println(cadenas[4]);
            } else {
            	errores();
                sc.close();
                throw new Exception(dlg + ERROR + " el cliente envió ERROR y no OK. " + REC + "-terminando.");
            }

            /***** Fase 6:  *****/
            linea = dc.readLine();
            //aca hubo cambios
            System.out.println(dlg + "recibio cc :-" + linea + "-continuado.");

            linea = dc.readLine();
            System.out.println(dlg + "recibio clave :-" + linea + "-continuado.");
            cadenas[5] = dlg + "recibio cc y clave - continuando";

            Random rand = new Random();
            int valor = rand.nextInt(1000000);
            String strvalor = valor+"";
            ac.println(strvalor);//aca hubo cambios
            cadenas[6] = dlg + "envio valor "+strvalor+" . continuado.";
            System.out.println(cadenas[6]);

            //byte [] hmac = S.hdg(strvalor.getBytes(), simetrica, algoritmos[3]);//aca hubo cambios
            ac.println(strvalor.hashCode());//aca tambien
            registrarCpu();
            long finalTransaccion = System.currentTimeMillis();
            registrarTiempo(finalTransaccion-comienzoTransaccion);
            System.out.println(dlg + "envio hmac cifrado. continuado.");

            cadenas[7] = "";
            linea = dc.readLine();
            if (linea.equals(OK)) {
                cadenas[7] = dlg + "Terminando exitosamente." + linea;
                System.out.println(cadenas[7]);
            } else {
            	errores();
                cadenas[7] = dlg + "Terminando con error" + linea;
                System.out.println(cadenas[7]);
            }
            sc.close();

            synchronized(file) {
                for (int i = 0; i < numCadenas; i++) {
                    escribirMensaje(cadenas[i]);
                }
                long tiempoTransaccion = finalTransaccion-comienzoTransaccion;
                escribirMensaje(" tiempo de transaccion promedio: " + acumuladoTiempoTransaccion/TOTAL_TRANS + " milisegundos ");
				escribirMensaje("errores totales : " + errores );
				escribirMensaje(" cpu load acumulado: " + acumuladoCPU/TOTAL_TRANS);
            }

        } catch (Exception e) {
        	errores();
            e.printStackTrace();
        }
    }
    
    public synchronized void registrarCpu() throws Exception {
		acumuladoCPU += getSystemCpuLoad();
	}
	public synchronized void registrarTiempo(long tiempo){
		acumuladoTiempoTransaccion += tiempo;
	}
	private synchronized void errores(){
		errores++;
	}

    public String toHexString(byte[] array) {
        return DatatypeConverter.printBase64Binary(array);
    }

    public byte[] toByteArray(String s) {
        return DatatypeConverter.parseBase64Binary(s);
    }

    public double getSystemCpuLoad() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
        if (list.isEmpty()) return Double.NaN;
        Attribute att = (Attribute)list.get(0);
        Double value = (Double)att.getValue();
        // usually takes a couple of seconds before we get real values
        if (value == -1.0) return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int)(value * 1000) / 10.0);
    }
}
