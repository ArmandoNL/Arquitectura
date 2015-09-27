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
public ArrayList<Integer> memTemp;
private int ciclosReloj; 
private int PC;
public final CyclicBarrier barrier;
private Semaphore semaforo;
private int quantum;
private int latencia;
private int tiempoBus;
private int cantHilos=2;
private Queue<Integer> vectPc;
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
           //quantum = ParInteger.parseInt(quantum.getText);
            ciclosReloj++;       
            for(int i = 0; i < 2; i++){
            	if(!comunicadores[i].ocupado){
                    int pcActual= vectPc.poll();
                    if(pcActual != -1){
                    
                        comunicadores[i].write(pcActual,quantum);
                        comunicadores[i].semaforoBuzon.release();
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
     
     public void botonEjecutar(ActionEvent e) {
  
        //Accion de open
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(HiloPrincipal.this);
  
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                 
                //Ahora lee el archivo y guarda sus contenidos en el array de hilos
                Charset charset = Charset.forName("US-ASCII");
                try(BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)){
                 String instruccion = null;
                 while((instruccion = reader.readLine()) != null){
                    System.out.println(instruccion);
                     
                    String[] instrucciones = instruccion.split(" ");
                    for(String s: instrucciones){
                        threadarray.add(Integer.parseInt(s));
                    }
                 }
                }
                catch(IOException exc){
                 System.err.println("IOException error");
                }
                
                log.append("Opening: " + file.getName() + "." + newline);
                cantHilos++;
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
  
        //Accion de ejecutar. Llama al metodo exec()
        } else if (e.getSource() == saveButton) {	
            try{
            	
                int cHilos = Integer.parseInt(caja.getText());
                if(cHilos == cantHilos){
                	System.out.println("Llamando a exec");
                    exec();
                }
                else{
                    log.append("La cantidad de hilos no es igual a los hilos proporcionados\n");
                 
                }
            }
            catch(Exception t){}
        }
    }
     
     public void llenarPC(String paht)
     {
         
     }

}
