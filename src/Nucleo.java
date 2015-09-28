import java.util.ArrayList;
import java.util.concurrent.*;

import javax.swing.*;


 
public class Nucleo implements Runnable {
    
  private int[] registros;  
  private int[] memoriaCompartida; // CAMBIAR ESTO!!!
  private int[] cacheDeDatos;
  private int[][] cacheDeInstrucciones;
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
  private int PC;
  private Comunicador[] comunicadores;
   private int quantum;
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
  
  public void traerBloque(int hpc)
  {
      int bloque = hpc/16;
      int j = (bloque*16)+4;
      int columCache = bloque%8;
      
      for(int palabra=1;palabra<=4;++palabra)
      {
          for(int i=bloque*16;i<j;i++)
          {
            cacheDeInstrucciones[i][columCache] = memoriaCompartida[i];
          }
      }
      cacheDeInstrucciones[16][columCache] = bloque;
  }
  
  public void run(){
  
    int[] vecInstruccion = new int[4];
    if(comunicadores[numProcesador].terminado) {
		this.PC = -1 ;
    }else{
        PC = comunicadores[numProcesador].read();
    }
	
	int hPC= PC;
	int numBloc = hPC/16;
	int blocCache= numBloc % 8;
	int i= hPC-numBloc*16;
	if(cacheDeInstrucciones[16][blocCache] != -1){
		for(int j= i; j<i+4;i++){
			int inst=0;
			vecInstruccion[inst] = cacheDeInstrucciones[j][blocCache];
			inst++;
		}
		hPC+=4;
		ejecutarInstruccion(vecInstruccion);
	}else{
		traerBloque(PC);
	
	}
  
  }

private void ejecutarInstruccion(int[] vector){
      System.out.println("Hilo " + numProcesador + ": leyendo instruccion con CP: " + contadorPrograma);
    int instruccion[] = new int[4];
        for(int i=0;i<4;i++){
        instruccion[i]=vector[i];
        }
    
    for(int i = 0; i < 4; i++){
        instruccion[i] = this.arrayInstrucciones.get(this.contadorPrograma);
        this.contadorPrograma++;
    }
    System.out.println("Se leyo instruiccion: " +instruccion[0]+" " +instruccion[1]+ " " +instruccion[2]+" " +instruccion[3]);
 
    switch(instruccion[0]){
      case 8:
          daddi(instruccion[2],instruccion[1],instruccion[3]);           
        break;
      case 32:
          dadd(instruccion[3],instruccion[1],instruccion[2]);
        break;
      case 34:
          dsub(instruccion[3],instruccion[1],instruccion[2]);
        break;
      case 4:
          beqz(instruccion[1], instruccion[3]);
        break;
      case 5:             
          bnez(instruccion[1], instruccion[3]);
        break;
      case 12:             
          dmul(instruccion[3],instruccion[1],instruccion[2]);
        break;
      case 14:             
          ddiv(instruccion[3],instruccion[1],instruccion[2]);
        break;
      case 3:             
          jal(instruccion[3]);
        break;
      case 2:             
          jr(instruccion[1]);
        break;
      case 63:
          fin();
        break;
      default:
        break;
    }    
}

 //hace una suma del valor del registro con un numero y lo guarda en un registro
  public void daddi(int regDestino, int regFuente, int numero){
     
    int valor = registros[regFuente]+numero;
    registros[regDestino]= valor;
    quantum--;
  }
  
  //Hace una suma de los valores de 2 registros y los guarda en un registro
  public void dadd(int regDestino, int regF1, int regF2){
    int valor = registros[regF1]+registros[regF2];
    registros[regDestino]= valor;
    quantum--;
  }
  
  //hace una resta de los valores de 2 registros y los guarda en un registro
  public void dsub(int regDestino, int regF1, int regF2){
      int valor = registros[regF1]-registros[regF2];
      registros[regDestino]= valor;
      quantum--;
  }
  
  //Si el valor es igual a 0 hace un salto
    public void beqz(int regComparacion, int salto){
         
        if(registros[regComparacion] == 0){
            contadorPrograma += salto*4;
        }
        quantum--;
    }
    
    //Si el valor del registro es diferente de 0 hace un salto
    public void bnez(int regComparacion, int salto){//segunda y cuarta parte, tercera vacia
        //System.out.println("valor "+registros[regComparacion]);
        if(registros[regComparacion] != 0){
            contadorPrograma += salto*4;
        }
        quantum--;
    }
    
    //
    public void jr(int regsalto){//segunda y cuarta parte, tercera vacia
        contadorPrograma =registros[regsalto];       
        quantum--;
    }
    
    //hace una multiplicacion de los valores de 2 registros y los guarda en un registro
    public void dmul(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = registros[regF1]-registros[regF2];
       registros[regDestino]= valor;
       quantum--;
        
    }
    
    //hace una division de los valores de 2 registros y los guarda en un registro
    public void ddiv(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = registros[regF1]-registros[regF2];
       registros[regDestino]= valor;
       quantum--;
    }
    
    //
    public void jal(int salto){//segunda y cuarta parte, tercera vacia
        registros[31]=contadorPrograma;
        contadorPrograma =contadorPrograma+salto;       
        quantum--;
    } 
    
   //Si el procesador llego al final del hilo, se desocupa e imprime los resultados
    public void fin(){
        comunicadores[numProcesador].ocupado = false;
        done = true;
        imprimirEstado();        
    }
    
    //imprime los resultados del hilo
    public void imprimirEstado(){
    	System.out.print("FIN de Hilo");
    }







}
