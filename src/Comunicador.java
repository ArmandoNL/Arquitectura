import java.util.concurrent.*;
 
public class Comunicador {
     
       public int hiloPC;
       public boolean ocupado;
       public boolean terminado;
       public Semaphore semaforoCache, semaforoComunicador;
       public int quantum;
       public int[] contexto;
       public int[] vectreg;

        
       public Comunicador(){
           contexto = new int[34];
           vectreg = new int[34];
           for(int i =0; i<34; i++){
               contexto[i]=0;
               vectreg[i] = 0;
           }
          
           semaforoCache = new Semaphore(1,true);
           semaforoComunicador = new Semaphore(1);
           terminado=false;
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
       
      //No se usan estos metodos porque se accesa directamente a los vectores 
       public int[] pedirContexto()
       {
           return contexto;
       }
       
       public void guardarContexto(int[] vec)
       {
           contexto = vec;
       }
        
       public int[] pedirRegistro()
       {
           return vectreg;
       }
       
       public int pedirCampoRegistro(int posicion)
       {
           return vectreg[posicion];
       }
              
       public void guardarCampoRegistro(int posicion,int numero)
       {
           vectreg[posicion] = numero;
       }
       
}
