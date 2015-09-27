import java.util.ArrayList;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.concurrent.*;

public class HiloControlador extends JPanel implements ActionListener {

public Comunicador[] comunicadores;
public Nucleo[] nucleos;
public ArrayList<Integer> memTemp;
private int ciclosReloj; 
private int PC;
public final CyclicBarrier barrier;
private Semaphore semaforo;
private int idHilos;
//private int quantum = ParInteger.parseInt(quantum.getText());
//private int tiempoEspera = ParInteger.parseInt(latenica.getText());
//private int tiempoBus = ParInteger.parseInt(tiempoBus.getText());
//public int latenciaa = 4*((2*tiempoBus)+tiempoEspera);
private int semaforoComunicador;
private int cantHilos = 2;
private Queue <Integer> vectPc;
JFileChooser fc;

  
    public HiloControlador() {
         
        ciclosReloj = 0;
        memTemp = new ArrayList<Integer>();
        PC = 0;
        barrier = new CyclicBarrier(cantHilos, barrierFuncion);
        fc = new JFileChooser();
    }
    
     Runnable barrierFuncion = new Runnable(){
        public void run(){   
            ciclosReloj++;       
            for(int i = 0; i < 2; i++){
            	if(!comunicadores[i].ocupado){
                    int pcActual= vectPc.poll();
                    if(pcActual != -1){
                        comunicadores[i].write(pcActual,quantum);
                        comunicadores[i].semaforoComunicador.release();
                    }
                    else{
                    	//si no hay hilos asignables, se le avisa al hilo que termino
                        comunicadores[i].write(-1, quantum);
                        comunicadores[i].terminado = true;
                        comunicadores[i].semaforoComunicador.release();
                    }
                }
            }
        }
        
        
        public void EjecutarActionPerformed(java.awt.event.ActionEvent evt){
            comunicadores = new Comunicador[2];
            for(int i = 0; i < 2; i++){
    		//System.out.println("Inicializando buzon " + i);
    		comunicadores[i] = new Comunicador();
            }
            nucleos = new Nucleo[2];
            for(int i = 0; i < 2; i++){
    		
    		nucleos[i] = new Nucleo(this, i, log);
            }
            if(vectPc.poll() != null){
            comunicadores[0].write(vectPc.poll(), quantum);
            comunicadores[1].write(vectPc.poll(), quantum);
            }else
            {
                System.out.println("Se acabo programa");
            }
    
            for(int i = 0; i < 3; i++){
    		System.out.println("HiloPrincipal: Iniciando el hilo " + i);
    		(new Thread(nucleos[i])).start();
            }    
        }
        
        
        
    };

     
     
     
     
     
}