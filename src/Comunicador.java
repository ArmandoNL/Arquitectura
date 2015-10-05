import java.util.concurrent.*;
 
public class Comunicador {
     
       public int hiloPC;
       public boolean ocupado;
       public boolean terminado;
       public Semaphore semaforoCache, semaforoComunicador;
       public int quantum;
       public int[] contexto;

        
       public Comunicador(){
           contexto = new int[34];
           for(int i =0; i<34; i++){
               contexto[i]=0;
           }
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
       
       public int readQ() {
           ocupado = true;
           return quantum;
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
