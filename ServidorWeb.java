import java.io.*;
import java.net.*;
import java.util.*;

public final class ServidorWeb {
   public static void main(String argv[]) throws Exception {
       int puerto = 7777;
    
       ServerSocket socketEscucha = new ServerSocket(puerto);
       
       while (true) {
           Socket socketConexion = socketEscucha.accept();
           SolicitudHttp solicitud = new SolicitudHttp(socketConexion);
           Thread hilo = new Thread(solicitud);
           hilo.start();
       }
   }
   
}

final class SolicitudHttp implements Runnable {
    final static String CRLF = "\r\n";
    private Socket socket;

    // Constructor
    public SolicitudHttp(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implementa el método run() de la interface Runnable.
    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proceseSolicitud() throws Exception {
        // Referencia al stream de salida del socket.
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Referencia y filtros para el stream de entrada.
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Recoge la línea de solicitud HTTP del mensaje.
        String lineaDeSolicitud = br.readLine();

        // Muestra la línea de solicitud en la pantalla.
        System.out.println();
        System.out.println(lineaDeSolicitud);

        // Recoge y muestra las líneas de header.
        String lineaDelHeader;
        while ((lineaDelHeader = br.readLine()).length() != 0) {
            System.out.println(lineaDelHeader);
        }



    // Extrae el nombre del archivo de la línea de solicitud.
    StringTokenizer partesLinea = new StringTokenizer(lineaDeSolicitud);
    partesLinea.nextToken();  // "salta" sobre el método, se supone que debe ser "GET"
    String nombreArchivo = partesLinea.nextToken();

    // Anexa un ".", de tal forma que el archivo solicitado debe estar en el directorio actual.
    nombreArchivo = "." + nombreArchivo;
    // Abre el archivo seleccionado.
    FileInputStream fis = null;
    boolean existeArchivo = true;
    try {
      fis = new FileInputStream(nombreArchivo);
    } catch (FileNotFoundException e) {
      existeArchivo = false;
    }
    // Construye el mensaje de respuesta.
    String lineaDeEstado = null;
    String lineaDeTipoContenido = null;
    String cuerpoMensaje = null;
    if (existeArchivo) {
        lineaDeEstado = "HTTP/1.1 200 OK" + CRLF;
        lineaDeTipoContenido = "Content-type: " + contentType(nombreArchivo) + CRLF;
    } else {
        lineaDeEstado = "HTTP/1.1 404 Not Found" + CRLF;
        lineaDeTipoContenido = "Content-type: text/html" + CRLF;
        cuerpoMensaje = "<HTML>" + 
                        "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                        "<BODY><b>404</b> Not Found</BODY></HTML>";
    }

    os.writeBytes(lineaDeEstado);

    os.writeBytes(lineaDeTipoContenido);
    
    os.writeBytes(CRLF);
    
    if (existeArchivo) {
        enviarBytes(fis, os);
        fis.close();
    } else {
        os.writeBytes(cuerpoMensaje);  
    }
    
        // Cierra los streams y el socket.
        os.close();
        br.close();
        socket.close();
    }

    private static void enviarBytes(FileInputStream fis, OutputStream os) throws Exception
    {
       // Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket.
       byte[] buffer = new byte[1024];
       int bytes = 0;
    
       // Copia el archivo solicitado hacia el output stream del socket.
       while((bytes = fis.read(buffer)) != -1 ) {
          os.write(buffer, 0, bytes);
       }
    }
    private static String contentType(String nombreArchivo)
    {
            if(nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
                    return "text/html";
            }
            if(nombreArchivo.endsWith(".gif")) {
                    return "image/gif";
            }
            if(nombreArchivo.endsWith(".jpeg")) {
                    return "image/jpeg";
            }
            return "application/octet-stream";
    }

}
