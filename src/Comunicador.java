import java.util.concurrent.*;
 
public class Comunicador {
     
       public int hiloPC;
       public boolean ocupado;
       public boolean terminado;
       public Semaphore semaforoCache, semaforoComunicador;
       public int quantum;
       public int[] contexto;

        
       public Comunicador(){
           semaforoCache = new Semaphore(1,true);
           semaforoComunicador = new Semaphore(1);
       }
        
       //para leer instrucciones, de uso exclusivo para el procesador y HP
       public void write(int contador, int miQuantum) {
           hiloPC = contador;
           quantum = miQuantum; 
        }
 
       
       public int read() {
           ocupado = true;
           return hiloPC;
       }
       
       public int[] pedirContexto()
       {
           return contexto;
       }
       
       public void guardarContexto(int[] vec)
       {
           contexto = vec;
       }
        
        
}
