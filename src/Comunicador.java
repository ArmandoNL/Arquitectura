import java.util.concurrent.*;
 
public class Comunicador {
     
       public int hiloPC;
       public boolean ocupado;
       public int numeroHilo;
       public Semaphore semaforoCache, semaforoBuzon;
       public int quantum;
        
       public Comunicador(){
           semaforoCache = new Semaphore(1,true);
           //semaforoDirectorio = new Semaphore(1,true);
           semaforoBuzon = new Semaphore(1);//OJO
           //semaforoBuzon.release();
       }
        
       //para leer instrucciones, de uso exclusivo para el procesador y HP
       public void write(int contador,int numHilo, int miQuantum) {
           hiloPC = contador;
           numeroHilo = numHilo;
           quantum = miQuantum; 
        }
 
       
       public int read() {
           ocupado = true;
           return hiloPC;
       }
        
        
}