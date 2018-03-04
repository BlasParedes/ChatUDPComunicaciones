/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatsistemasi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import org.json.simple.JSONObject;

/**
 *
 * @author usuario
 */
public class Privado extends JFrame{
    private DatagramSocket Socket;
    private JTextArea Texto = new JTextArea();
    private JTextArea Mensaje = new JTextArea();
    private final int PuertoSalida = 20000;
    private int PuertoEntrada = 20001;
    private DatagramPacket Salida;
    private JButton Enviar = new JButton("Enviar");
    public Privado(int Height, int Width, String Nombre, DatagramSocket Socket){
        super(Nombre);
        this.Socket = Socket;
         this.setSize(Width, Height); 
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(null);
        
        this.setSize(200, 100);
        this.Enviar.setBounds(Width - 125,Height-80,100,40);
        this.Enviar.addActionListener(new Listener());
        this.add(this.Enviar);
        this.Mensaje.setBounds(10, Height-80, Width - 145, 40);
        this.add(this.Mensaje);
        this.Texto.setBounds(10,10,Width - 200,Height-100);
        this.Texto.setFocusable(false);
        this.add(this.Texto);        
        this.setVisible(true);
        
        
    }
    private class Listener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            byte buffer[] = new byte[100];
            String entrada = Mensaje.getText();            
            Mensaje.setText("");
            JSONObject json = new JSONObject();
            json.put("Tipo", "Normal");
            
           
            try {
                json.put("IP", InetAddress.getLocalHost().getHostAddress().toString());
                json.toString().getBytes(0, json.toString().length(), buffer, 0);
                //Salida = new DatagramPacket(buffer,json.toString().length(),IP,PuertoSalida);
                Socket.send(Salida);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    };
}
