
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatsistemasi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**
 *
 * @author usuario
 */
public class Frame extends JFrame{
    private JTextArea Texto = new JTextArea();
    private JTextArea Mensaje = new JTextArea();
    private JButton Enviar = new JButton("Enviar");
    private Hilo hilo = new Hilo();
    private JList ListaNombre = new JList();
    private String Nombre;
    private String Host = "localhost";
    private DefaultListModel modelo = new DefaultListModel();
    private DatagramSocket socket = null;
    private DatagramPacket Salida;
    private DatagramPacket Entrada;
    private final int PuertoSalida = 20000;
    private int PuertoEntrada = 20001;
    private String chat;
    private InetAddress IPServer = null;
    private ArrayList<InetAddress> ListIP = new ArrayList<>();
    private JScrollPane scroll;
    public Frame(int Height,int Width,String title) throws UnknownHostException {
        super(title);
        int band=0, valido=0;
        JSONObject json = new JSONObject();
        byte buffer[] = new byte[100];
        try {
            socket = new DatagramSocket( PuertoEntrada);
            System.out.println(socket.getBroadcast());
            socket.setBroadcast(true);
        } catch (SocketException ex) {
            System.out.println("problema");          
        }
        band = 0;
        hilo.start();
        band=0;
        modelo.addElement("Usuario 1");
        modelo.addElement("Usuario 2");
        modelo.addElement("Usuario 3");
        modelo.addElement("Usuario 4");
        modelo.addElement("Usuario 5");
        ListIP.add(InetAddress.getByName("0.0.0.0"));
        ListIP.add(InetAddress.getByName("0.0.0.0"));
        ListIP.add(InetAddress.getByName("0.0.0.0"));
        ListIP.add(InetAddress.getByName("0.0.0.0"));
        ListIP.add(InetAddress.getByName("0.0.0.0"));
        do{
            valido = 1;
            this.Nombre = JOptionPane.showInputDialog(null, "Diga su nombre");
            try{
            this.chat = JOptionPane.showInputDialog(null, "Seleccione su chat", "Chats Abiertos", JOptionPane.QUESTION_MESSAGE, null, new Object[]{ "chat 1", "chat 2", "chat 3", "chat 4", "chat 5"},"Seleccione").toString();
            }catch(NullPointerException ex){
                valido = 0;
            }
            if(this.Nombre == null)
                valido = 0;
            if(valido == 0)
                JOptionPane.showConfirmDialog(null, "Error Con los datos ingresados", "ERROR", JOptionPane.ERROR_MESSAGE);
        }while(valido == 0);
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        json.put("tipo","login");
        json.put("nickname", this.Nombre);
        json.put("sala", this.chat);
        json.put("IP", InetAddress.getLocalHost().getHostAddress().toString());
        json.toString().getBytes(0, json.toString().length(), buffer, 0);
        Salida = new DatagramPacket(buffer,json.toString().length(),IPServer,PuertoSalida);
        try {
            socket.send(Salida);
        } catch (IOException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            System.out.println("Error el socket: " + ex);
        }
        this.ListaNombre.addListSelectionListener(new Chat());
        this.setTitle("Sala: " + this.chat + " - Usuario: " + this.Nombre);
        this.setSize(Width, Height); 
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(null);
        this.ListaNombre.setModel(modelo);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        this.ListaNombre.setBackground(Color.white);
        this.ListaNombre.setBounds(Width - 175, 10, Width - 540, Height-100);
        this.Enviar.setBounds(Width - 125,Height-80,100,40);
        this.Enviar.addActionListener(new Listener());
        this.add(this.Enviar);
        this.Mensaje.setBounds(10, Height-80, Width - 145, 40);
        this.add(this.Mensaje);
        scroll = new JScrollPane (this.Texto);
        
        scroll.setBounds(30, 30, 100, 200);
        this.Texto.setBounds(10,10,Width - 200,Height-100);
        this.Texto.setFocusable(false);        
        this.add(this.Texto);
        this.getContentPane().add(scroll, BorderLayout.CENTER);
        this.add(scroll);
        this.add(this.ListaNombre);
        this.setVisible(true);
        //this.hilo.run();
    }
    public void sendPrivado(String Nombre, InetAddress IP, String Mensaje){
        byte buffer[] = new byte[100];
        String mensaje;
        if(Mensaje.equals("")){
            mensaje = JOptionPane.showInputDialog(null, "ingrese su mensaje privado", "Mensaje para " + Nombre, JOptionPane.DEFAULT_OPTION);
        }else{
            mensaje = JOptionPane.showInputDialog(null, Nombre + ": " + Mensaje, "Mensaje para " + Nombre, JOptionPane.DEFAULT_OPTION);
        }
        
        JSONObject json = new JSONObject();
        json.put("Tipo", "sendPrivado");
        json.put("nickname", this.Nombre);
        json.put("sala", this.chat);
        json.put("nombreobjetivo", Nombre);
        json.put("IPobjetivo", IP);    
         try {    
            json.put("IP", InetAddress.getLocalHost().getHostAddress().toString());
            json.toString().getBytes(0, json.toString().length(), buffer, 0);
            Salida = new DatagramPacket(buffer,json.toString().length(),IPServer,PuertoSalida);
            socket.send(Salida);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    private class Cerrar implements WindowListener{

        @Override
        public void windowOpened(WindowEvent e) {
           }

        @Override
        public void windowClosing(WindowEvent e) {
            byte buffer[] = new byte[100];
            JSONObject json = new JSONObject();
            json.put("tipo", "logout");
            json.put("sala", chat);
            json.put("nickname", Nombre);
            try {
                json.put("IP", InetAddress.getLocalHost().getHostAddress().toString());
                json.toString().getBytes(0, json.toString().length(), buffer, 0);
                Salida = new DatagramPacket(buffer,json.toString().length(),IPServer,PuertoSalida);
                socket.send(Salida);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }finally{ 
                System.exit(0);
            }
        }

        @Override
        public void windowClosed(WindowEvent e) {
            }

        @Override
        public void windowIconified(WindowEvent e) {
            }

        @Override
        public void windowDeiconified(WindowEvent e) {
            }

        @Override
        public void windowActivated(WindowEvent e) {
            }

        @Override
        public void windowDeactivated(WindowEvent e) {
            }
    
    }
    private class Listener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            byte buffer[] = new byte[100];
            String entrada = Mensaje.getText();            
            Mensaje.setText("");
            JSONObject json = new JSONObject();
            json.put("tipo", "msgclient");
            json.put("mensaje", entrada);
            json.put("sala", chat);
            json.put("nickname", Nombre);
            try {
                json.put("IP", InetAddress.getLocalHost().getHostAddress().toString());
                json.toString().getBytes(0, json.toString().length(), buffer, 0);
                Salida = new DatagramPacket(buffer,json.toString().length(),IPServer,PuertoSalida);
                socket.send(Salida);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    };
    
    private class Hilo extends Thread{   
        JSONObject json = new JSONObject();
        public Hilo() {            
        }
        @Override
        public void run(){            
            while(true){
                byte buffer[] = new byte[100];
                Entrada = new DatagramPacket( buffer, 100 );                
                try {
                    socket.receive(Entrada);                    
                    String Mensaje = new String(Entrada.getData());                    
                    Mensaje = Mensaje.substring(0, Mensaje.indexOf("}")+1);
                    JSONParser parser = new JSONParser();                    
                    json = (JSONObject) parser.parse(Mensaje);
                    String Tipo = (String) json.get("tipo");
                    System.out.println("MENSAJE : ["+Mensaje+"]");
                    if( Tipo.equals("mensaje") ){
                        Mensaje = (String) json.get("mensaje");
                        Texto.setText(Texto.getText() + "\n" + Mensaje);
                    }else if( Tipo.equals("adduser") ){
                        modelo.addElement(json.get("nickname") + ": " + json.get("IP"));
                        ListIP.add(InetAddress.getByName((String) json.get("IP")));
                        ListaNombre.setModel(modelo);
                    }else if( Tipo.equals("servidor") ){                                             
                        if( IPServer == null){
                            System.out.println("IP : [" + json.get("IP") + "]");   
                            IPServer = InetAddress.getByName((String) json.get("IP"));
                        }
                    }else if(Tipo.equals("deluser")){
                        modelo.removeElement(json.get("nickname") + ": " + json.get("IP"));
                        ListIP.remove(InetAddress.getByName((String) json.get("IP")));
                        ListaNombre.setModel(modelo);
                    }else if(Tipo.equals("sendPrivado")){                        
                        sendPrivado((String) json.get("nombre"),(InetAddress) json.get("IP"),(String) json.get("mensaje"));
                    }               
                } catch (IOException ex ) {
                    System.out.println("Error: " + ex.toString());
                }catch (ParseException ex) {
                    System.out.println("Error: " + ex);
                } 
            }
        }
    };    
    private class Chat implements ListSelectionListener{

        @Override
        public void valueChanged(ListSelectionEvent e) { 
            sendPrivado((String) modelo.get(e.getFirstIndex()), ListIP.get(e.getFirstIndex()), "");
        }
       
    };
}