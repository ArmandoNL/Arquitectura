import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;
 
public class Comunicador {
     
    //variables que se van a compartir entre el HiloControlador y los núcleos.
       public int hiloPC;
       public boolean ocupado;
       public boolean terminado;
       public Semaphore semaforoCache, semaforoComunicador;
       public int quantum;
       public int[] vectreg;
       public boolean seguir;
       public int pcFinal;
       public boolean cambiarCiclo;
       public ArrayList<int[]> contextos;

        
       public Comunicador(){
           vectreg = new int[34];
           contextos = new ArrayList<int[]>();
          
           semaforoCache = new Semaphore(1,true);
           semaforoComunicador = new Semaphore(1);
           terminado=false;
           seguir=false;
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
           int[] vec = contextos.get(0);
           contextos.remove(0);
           return vec;
       }
       
       public void guardarContexto(int[] vec)
       {
           contextos.add(vec);
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
       
       public void setPcFinal(int pcf)
       {
           pcFinal = pcf;
       }
       
       public int getPcFinal()
       {
           return pcFinal;
       }
               
}
