/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 *
 * @author usuario
 */
public class ServidorComunicacionesI {

    /**
     * @param args the command line arguments
     */
    private DatagramSocket socket = null;
    private DatagramPacket Entrada;
    private DatagramPacket Salida;
    private int puertoEntrada = 20000;
    private final int PuertoSalida = 20001;
    private InetAddress IP;
    private Hilo hilo;
    private ArrayList<InetAddress> []Salas;
    private ArrayList<String> []Users;
    public ServidorComunicacionesI() throws UnknownHostException, IOException{
        /*JSONObject  jose = new JSONObject ();*/
        byte buffer[] = new byte[100]; 
        Salas = new ArrayList[5];
        for( int i=0 ; i<5 ; i++)
            Salas[i] = new ArrayList<>();
        Users = new ArrayList[5];
        for( int i=0 ; i<5 ; i++)
            Users[i] = new ArrayList<>();
        try {
            socket = new DatagramSocket( puertoEntrada );
        } catch (SocketException ex) {
            System.out.println("Error: " + ex.toString());       
        }
        hilo = new Hilo();
        hilo.start();
        System.out.println("Esperando Mensajes...");
        this.IP = InetAddress.getByName("localhost");
        while(true){
            Entrada = new DatagramPacket( buffer, 100 );
            try {
                socket.receive(Entrada);
            } catch (IOException ex) {
                System.out.println("Error: " + ex.toString());
            }
            String Mensaje = new String(Entrada.getData());
            Mensaje = Mensaje.substring(0, Entrada.getLength());
            System.out.println( Entrada.getAddress() + ": " + 
                            Entrada.getPort() + " Mensaje: " + Mensaje + 
                            " Tamano: " + Entrada.getLength());
            JSONObject json = new JSONObject();
            JSONParser parser = new JSONParser();
            
            try {
                json = (JSONObject) parser.parse(Mensaje); 
                int Sala = getSala( (String)json.get("sala") ) ;   
                if(json.get("tipo").equals("login")){
                    byte buffer2[] = new byte[100]; 
                    JSONObject json2 = new JSONObject();
                    json2.put("tipo", "adduser");
                    json2.put("nickname",json.get("nickname"));
                    json2.put("IP",json.get("IP"));
                    json2.put("sala", (String)json.get("sala"));
                    json2.toString().getBytes(0, json2.toString().length(), buffer2, 0);                    
                    for(int i=0; i<Salas[Sala].size() ;i++){
                        Salida = new DatagramPacket(buffer2,json2.toString().length(),Salas[Sala].get(i),PuertoSalida);
                        System.out.println("Sala [" + Sala + "] - IP [" + Salas[Sala].get(i) + "] Tamaño: " + Salas[Sala].size());
                        socket.send(Salida);
                    }
                    for(int i=0; i<Salas[Sala].size() ;i++){
                        json2.put("tipo", "adduser");
                        json2.put("nickname",Users[ Sala ].get(i));
                        json2.put("IP",Salas[ Sala ].get(i));
                        json2.put("sala", (String)json.get("sala"));
                        json2.toString().getBytes(0, json2.toString().length(), buffer2, 0);
                        Salida = new DatagramPacket(buffer2,json2.toString().length(),InetAddress.getByName((String) json.get("IP")),PuertoSalida);
                        //System.out.println("Sala [" + Sala + "] - IP [" + Salas[Sala].get(i) + "] Tamaño: " + Salas[Sala].size());
                        socket.send(Salida);
                    }
                    if(! Salas[ Sala ].contains(InetAddress.getByName((String) json.get("IP")))){
                        Salas[ Sala ].add(InetAddress.getByName((String) json.get("IP")));
                        Users[ Sala ].add((String) json.get("nickname"));
                    }
                }else if(json.get("tipo").equals("msgclient")){
                    byte buffer2[] = new byte[100]; 
                    Mensaje = json.get("nensaje").toString();
                    Mensaje = "<" + json.get("nickname") + ">:_  " + Mensaje;
                    JSONObject json2 = new JSONObject();
                    json2.put("tipo", "mensaje");
                    json2.put("mensaje",Mensaje);
                    json2.put("IP",json.get("IP"));
                    json2.put("sala", (String)json.get("sala"));
                    json2.toString().getBytes(0, json2.toString().length(), buffer2, 0);                                     
                    for(int i=0; i<Salas[Sala].size() ;i++){
                        Salida = new DatagramPacket(buffer2,json2.toString().length(),Salas[Sala].get(i),PuertoSalida);
                        System.out.println("Sala [" + Sala + "] - IP [" + Salas[Sala].get(i) + "]" + Salas[Sala].size());
                        socket.send(Salida);
                    }
                    
                }else if(json.get("tipo").equals("logout")){
                    byte buffer2[] = new byte[100]; 
                    JSONObject json2 = new JSONObject();
                    Salas[ Sala ].remove(InetAddress.getByName((String) json.get("IP")));
                    Users[ Sala ].remove((String) json.get("nickname"));
                    for(int i=0; i<Salas[Sala].size() ;i++){
                        json2.put("tipo", "deluser");
                        json2.put("nickname",json.get("nickname"));
                        json2.put("IP",json.get("IP"));
                        json2.put("sala", (String)json.get("sala"));
                        json2.toString().getBytes(0, json2.toString().length(), buffer2, 0);
                        Salida = new DatagramPacket(buffer2,json2.toString().length(),Salas[Sala].get(i),PuertoSalida);
                        //SSalida = new DatagramPacket(buffer2,json2.toString().length(),Salas[Sala].get(i),PuertoSalida);ystem.out.println("Sala [" + Sala + "] - IP [" + Salas[Sala].get(i) + "] Tamaño: " + Salas[Sala].size());
                        socket.send(Salida);
                    }                    
                }else if(json.get("tipo").equals("sendPrivado")){
                    byte buffer2[] = new byte[100];
                    Mensaje.getBytes(0, Mensaje.length(), buffer2, 0);
                    Salida = new DatagramPacket(buffer2,Mensaje.length(), (InetAddress) json.get("IPObjetivo"),PuertoSalida);
                    socket.send(Salida);
                }
            } catch (org.json.simple.parser.ParseException ex) {
                System.out.println("ParseException " + ex);
            }
            
        }
    }
    public int getSala(String NameSala){
        if(NameSala.equals("chat 1")){
            return 0;                     
        }else if(NameSala.equals("chat 2")){
            return 1;
        }else if(NameSala.equals("chat 3")){
            return 2;
        }else if(NameSala.equals("chat 4")){
            return 3;
        }else if(NameSala.equals("chat 5")){
            return 4;
        }
        return -1;
    }
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        new ServidorComunicacionesI();
    }
    private class Hilo extends Thread{     
        DatagramSocket enviador = null;
        private InetAddress BroadCast;
        public Hilo() {
            try {
                System.out.println("ENTRA");
                BroadCast = InetAddress.getByName("200.109.150.182");
                enviador = new DatagramSocket();
                enviador.setBroadcast(true);
            } catch (SocketException ex) {
                System.err.println("SocketException - Constructor");
            } catch (UnknownHostException ex) {
                System.err.println("UnknownHostException - Constructor");
            }
        }
        @Override
        public void run(){
            byte [] dato = new byte [100];
            // El destinatario es 192.20.20.255, que es la dirección de broadcast
            DatagramPacket dgp;   
            JSONObject json = new JSONObject();
            try {
                    json.put("tipo", "servidor");
                json.put("IP", InetAddress.getLocalHost().getHostAddress());
                json.toString().getBytes(0, json.toString().length(), dato, 0);
                while(true){                    
                    dgp = new DatagramPacket(dato, dato.length, BroadCast, PuertoSalida);
                    enviador.send(dgp);  
                    //System.out.println("Enviado: " + LocalIP);
                    Thread.sleep(1000);
                }
            } catch (IOException ex) {
                System.err.println("IOException: " + ex);
            } catch (InterruptedException ex) {
                System.err.println("InterruptedException: " + ex);
            }
        }
    };
    
}
