import java.util.ArrayList;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.awt.*;
import java.awt.event.*;
 
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.concurrent.*;

public class HiloControlador extends JPanel implements ActionListener {

public Comunicador[] comunicadores;
public ArrayList<Integer> memTemp;
private int ciclosReloj; 
private int PC;
public final CyclicBarrier barrier;
private Semaphore semaforo;
private int quantum;
private int latencia;
private int tiempoBus;
private int cantHilos=2;
private ArrayList<Integer> vectPc;
JFileChooser fc;

  
    public HiloControlador() {
         
        ciclosReloj = 0;
        memTemp = new ArrayList<Integer>();
        vectPc = new ArrayList<Integer>();
        PC = 0;
        barrier = new CyclicBarrier(cantHilos, barrierFuncion);
        fc = new JFileChooser();
    }
    
     Runnable barrierFuncion = new Runnable(){
        public void run(){
            
            ciclosReloj++;       
            for(int i = 0; i < 2; i++){
            	if(!buzones[i].ocupado){
                   
                    int pcAsignado = asignarPC();
                    if(pcAsignado != -1){
                    	log.append("Hilo " + i + " lee hilo num " + numHilo);
                        buzones[i].write(pcAsignado,numHilo);
                        buzones[i].semaforoBuzon.release();
                        System.out.println("Semaforo del hilo " + i + " liberado.");
                    }
                    else{
                    	//si no hay hilos asignables, se le avisa al hilo que termino
                        buzones[i].write(-1, numHilo);
                        buzones[i].done = true;
                        buzones[i].semaforoBuzon.release();
                        //System.out.println("Hilo " + i + " ha terminado.");
                    }
                     
                }
            }
            
             
        }
    };

}