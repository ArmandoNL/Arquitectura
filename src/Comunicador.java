import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;
 
public class Comunicador {
     
    //variables que se van a compartir entre el HiloControlador y los núcleos.
       public int hiloPC;
       public boolean ocupado;
       public boolean terminado;
       public Semaphore semaforoCache;
       public int quantum;
       public int[] vectreg;
       public boolean seguir;
       public int pcFinal;
       public boolean cambiarCiclo;
       //public ArrayList<int[]> contextos;
       
        
       public Comunicador(){
           vectreg = new int[34];
           for(int i=0; i<34; i++){
               vectreg[i]=0;
           }
          
           //contextos = new ArrayList<int[]>();
           //busCacheInst = new Semaphore(1);
           //busCacheDatos = new Semaphore(1);
           semaforoCache = new Semaphore(1);
           terminado=false;
           seguir=false;
       }
        
       //para leer instrucciones, de uso exclusivo para el procesador y HP
       public void write(int contador, int miQuantum) { //asigna el valor del PC y el quantum
           hiloPC = contador;
           quantum = miQuantum; 
        }
       
       //se encarga de asignar el valor del PC
       public int read() {
           ocupado = true;
           return hiloPC;
       }
       
       //se encarga de asignar el valor del quantum
       public int readQ() {
           ocupado = true;
           return quantum;
       }
       
      //retorna un vector con un los valores del contexto y los borra de la cola de contextos.
       /*public int[] pedirContexto()
       {
           int[] vec = contextos.get(0);
           contextos.remove(0);
           return vec;
       }
       
       //guarda en la cola de contextos, los registros.
       public void guardarContexto(int[] vec)
       {
           contextos.add(vec);
       }*/
       
       //Devuelve el campo del registro solicitado
       public int pedirCampoRegistro(int posicion)
       {
           return vectreg[posicion];
       }
       
       //se encarga de asignar el PC donde termina el archivo
       public void setPcFinal(int pcf)
       {
           pcFinal = pcf;
       }
       
       //retorna el valor del PC donde se termina el archivo
       public int getPcFinal()
       {
           return pcFinal;
       }
               
}
