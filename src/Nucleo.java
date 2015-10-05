import java.util.ArrayList;
import java.util.concurrent.*;

public class Nucleo implements Runnable {
    
  private final int[] registros;  
  private int[] memoriaCompartida; 
  private final int[][] cacheDeInstrucciones;
  private HiloControlador mainThread;
  private ArrayList<Integer> arrayInstrucciones; //donde se inicializa??
  private CyclicBarrier barrera;
  private int contadorPrograma;
  private int cicloReloj;
  private boolean terminar;
  private final int numProcesador;
  private boolean instruccionCompletada;
  private int PC;
  private int hPC;
  private boolean primerLeido;
  private Comunicador[] comunicadores;
  private int quantumNucleo;
  boolean busOcupado;
  //public Directorio directorio;
  
  //nuevo constructor del procesador
  public Nucleo(HiloControlador hc, int id){	  
	 
          this.numProcesador = id;
          mainThread = hc;
          //terminar = true;
          barrera = mainThread.barrier;
          comunicadores = mainThread.comunicadores;
          registros = new int[33];
	    for(int i = 0; i < 33; i++){
	      registros[i] = 0;
	    }
          cacheDeInstrucciones = new int[17][8];
	    for(int i = 0; i < 8; i++){                
	      cacheDeInstrucciones[16][i] = -1;
	    }
	quantumNucleo = comunicadores[numProcesador].readQ();
	
  }
  
  public void run(){
    obtenerPC();
    while(true){
        if(estaenCache(hPC)){
            recuperarDeCache();
        }else{
            falloCache();
        }
        
        
    }
}
  
  /*
   public void limpiarcache(){
        for(int i = 0; i < 8; i++){                
            cacheDeInstrucciones[16][i] = -1;
        }
  }
  */
  
 /*Que pasa cuando ya todos los -1 de cada bloque se cambiaron?? no se podria revisar que si es -1 la proxima vez
  tiene que existir un limpiar cache que los vuelva a poner en -1 y asi se pueda seguir revisando si el bloque esta
  */  
  public boolean estaenCache(int hpc){
       int bloque = hpc/16;
       int columCache = bloque%8;
       return cacheDeInstrucciones[16][columCache]==bloque;  
  }
  
  public void traerBloque()
  {
      int bloque = hPC/16;
      int j = (bloque*16)+16;
      int columCache = bloque%8;
      int fila = 0;
      for(int i=bloque*16;i<j;i++)
      {
         cacheDeInstrucciones[fila][columCache] = arrayInstrucciones.get(i);
         fila++;
      }
      cacheDeInstrucciones[16][columCache] = bloque;
  }
  

private void buscarEnCache(){
        int[] vecInstruccion = new int[4];
	int numBloc = hPC/16;
	int blocCache= numBloc % 8;
	int i= hPC-(numBloc*16);
	int inst=0;
        for(int j= i; j<i+4; i++){
            vecInstruccion[inst] = cacheDeInstrucciones[j][blocCache];
            inst++;
        }
	hPC+=4;
	ejecutarInstruccion(vecInstruccion);
        cambiarCiclo();
        if(quantumNucleo != 0)
        {
            cambiarCiclo();
        }
        else
        {
            seAcaboQuantum();
        }
	
}

private void seAcaboQuantum()
{
    contexto();
    int pcActual = mainThread.vectPc.poll();
    this.comunicadores[this.numProcesador].write(pcActual, quantumNucleo);
    obtenerPC();
}
 
private void obtenerPC(){
    if(comunicadores[numProcesador].terminado){
        this.PC = -1 ;
     }else{
        PC = comunicadores[numProcesador].read();
        hPC=PC;
        if(this.comunicadores[this.numProcesador].contexto[33]==hPC){
            int[] vectContexto = new int[34];
            vectContexto= this.comunicadores[this.numProcesador].contexto;
            cambiarRegistro(vectContexto);
        }
     }
}


private void cambiarRegistro(int [] vec){
     for(int i = 0; i<33; i++){
         registros[i] = vec[i];
     }

}

boolean pedirBus(){ //devuelve verdadero si se pudo obtener el bus, falso si está ocupado.
    if(this.comunicadores[this.numProcesador].semaforoCache.tryAcquire()){
        busOcupado = true;
        return true;
    }else{
        return false;
    }
}

boolean liberarBus(){ //libera el bus una vez que no se necesita.
    this.comunicadores[this.numProcesador].semaforoCache.release();
    busOcupado = false;
    return true;
}

private void falloCache(){ //en caso 
    primerLeido= false;
    if(pedirBus()){
        traerBloque();
        int i=0;
        while(i<mainThread.latencia){
            cambiarCiclo();
            i++;
        }
        liberarBus();
        cambiarCiclo();
    }else{
        while(!pedirBus()){
            cambiarCiclo();
        }
    }
}

private void cambiarCiclo(){
    try{
        barrera.await();
    }catch (InterruptedException | BrokenBarrierException e){}
    
    cicloReloj++;
}

public int[] contexto()
    {
        int[] vec = new int[34];
        for(int i = 0; i<34; i++)
        {
            vec[i] = this.registros[i];
        }
        vec[33] = this.PC;
       
        //AGREGAR PC DE INICIO DE ARCHIVO A LA COLA DE PC.s
        mainThread.vectPc.add(PC);
        return vec; 
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
    quantumNucleo--;
  }
  
  //Hace una suma de los valores de 2 registros y los guarda en un registro
  public void dadd(int regDestino, int regF1, int regF2){
    int valor = registros[regF1]+registros[regF2];
    registros[regDestino]= valor;
    quantumNucleo--;
  }
  
  //hace una resta de los valores de 2 registros y los guarda en un registro
  public void dsub(int regDestino, int regF1, int regF2){
      int valor = registros[regF1]-registros[regF2];
      registros[regDestino]= valor;
      quantumNucleo--;
  }
  
  //Si el valor es igual a 0 hace un salto
    public void beqz(int regComparacion, int salto){
         
        if(registros[regComparacion] == 0){
            contadorPrograma += salto*4;
        }
        quantumNucleo--;
    }
    
    //Si el valor del registro es diferente de 0 hace un salto
    public void bnez(int regComparacion, int salto){//segunda y cuarta parte, tercera vacia
        //System.out.println("valor "+registros[regComparacion]);
        if(registros[regComparacion] != 0){
            contadorPrograma += salto*4;
        }
        quantumNucleo--;
    }
    
    //
    public void jr(int regsalto){//segunda y cuarta parte, tercera vacia
        contadorPrograma =registros[regsalto];       
        quantumNucleo--;
    }
    
    //hace una multiplicacion de los valores de 2 registros y los guarda en un registro
    public void dmul(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = registros[regF1]-registros[regF2];
       registros[regDestino]= valor;
       quantumNucleo--;
        
    }
    
    //hace una division de los valores de 2 registros y los guarda en un registro
    public void ddiv(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = registros[regF1]-registros[regF2];
       registros[regDestino]= valor;
       quantumNucleo--;
    }
    
    //
    public void jal(int salto){//segunda y cuarta parte, tercera vacia
        registros[31]=contadorPrograma;
        contadorPrograma =contadorPrograma+salto;       
        quantumNucleo--;
    } 
    
   //Si el procesador llego al final del hilo, se desocupa e imprime los resultados
    public void fin(){
        comunicadores[numProcesador].ocupado = false;
        terminar = true;
        imprimirEstado();        
    }
    
    //imprime los resultados del hilo
    public void imprimirEstado(){
    	System.out.print("FIN de Hilo");
    }

  


}
