import java.util.ArrayList;
import java.util.concurrent.*;

import javax.swing.*;


 
public class Nucleo implements Runnable {
    
  private int[] registros;  
  private int[] memoriaCompartida;
  private int[] cacheDeDatos;
  private int[] etiquetaBloqueCache;
  private char[] estadoBloqueCache;
  private HiloControlador mainThread;
  private ArrayList<Integer> arrayInstrucciones;
  private CyclicBarrier barrera;
  private int contadorPrograma;
  private Comunicador comunicador;
  private int cicloReloj;
  private int cicloAtrasado;
  private boolean done;
  private JTextArea log;//borrar
  private Semaphore semaforoBuzon;//borrar 
  private int relojInicial;
  private int numProcesador;
  private boolean directorioSolicitado;
  private int numDirectorioSolicitado;
  private boolean instruccionCompletada;
  private boolean cacheSolicitada;
  
  private Comunicador[] comunicadores;
  //public Directorio directorio;
  
  //nuevo constructor del procesador
  public Nucleo(HiloControlador hp, int id){
	  
	  this.numProcesador = id;
	 
	 /* mainThread = hp;
	  buzones = mainThread.buzones;
	  barrera = mainThread.barrier;
	  arrayInstrucciones = mainThread.threadarray;
	 // log = l;
	  //directorio = new Directorio(id);
	  registros = new int[32];
	    for(int i = 0; i < 32; i++){
	      registros[i] = 0;
	    }
	    memoriaCompartida = new int[32];
	    for(int i = 0; i < 32; i++){
	      memoriaCompartida[i] = 0;
	    }
	    etiquetaBloqueCache = new int[4];
	    for(int i = 0; i < 4; i++){
	        etiquetaBloqueCache[i] = -1;
	      }
	    estadoBloqueCache= new char[4];
	    for(int i = 0; i < 4; i++){
	        estadoBloqueCache[i] = 'I';
	      }
	    cacheDeDatos = new int[16];
	    for(int i = 0; i < 16; i++){
	      cacheDeDatos[i] = 0;
	    }
	    done = true;
	    relojInicial=0;
	    instruccionCompletada = true;
	    cicloAtrasado = 0;
	    directorioSolicitado = false;
 		numDirectorioSolicitado = -1;
	  //creo que eso es todo?*/
  }
  
  public void run(){}
  
  
}
