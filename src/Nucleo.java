import java.util.ArrayList;
import java.util.concurrent.*;

public class Nucleo implements Runnable {
    
 // private final int[] registros;  
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
  private int pcFinal;
  private int hPC;
  private boolean primerLeido;
  private Comunicador[] comunicadores;
  private int quantumNucleo;
  boolean busOcupado;
  boolean finalizar = false;
  //public Directorio directorio;
  
  //nuevo constructor del procesador
  public Nucleo(HiloControlador hc, int id){	  
	 
          this.numProcesador = id;
          mainThread = hc;
          //terminar = true;
          arrayInstrucciones = mainThread.memTemp;
          barrera = mainThread.barrier;
          comunicadores = mainThread.comunicadores;          
            this.cacheDeInstrucciones = new int[17][8];
	    for(int i = 0; i < 8; i++){                
	    this.cacheDeInstrucciones[16][i] = -1;
	    }
	
	
  }
  
  @Override
  public void run(){
    obtenerPC();
    while(!this.comunicadores[this.numProcesador].terminado){
        if(estaenCache(hPC)){
            recuperarDeCache();
        }else{
            falloCache();
        }
    }
       
    while(!finalizar){
        cambiarCiclo();
        if(comunicadores[0].hiloPC==-1 && comunicadores[1].hiloPC==-1){
             finalizar=true;
        }
    } 
      
   System.out.println("EL quantum final es :" + quantumNucleo);
  
   System.out.println("se terminooo");
}
   
  public boolean estaenCache(int hpc){
       int bloque = hpc/16;
       int columCache = bloque%8;
      return this.cacheDeInstrucciones[16][columCache] == bloque; 
  }
  
  public void imprimir(){
        System.out.println("SOY HILO " + this.numProcesador);
          for(int i =0; i<33; i++){
               System.out.println("Reg" + i + "=" + comunicadores[this.numProcesador].pedirCampoRegistro(i));             
           }
  }
  
  public void traerBloque()
  {
      int bloque = this.hPC/16;
      int j = (bloque*16)+16;
      int columCache = bloque%8;
      int fila = 0;
      for(int i=bloque*16;i<j;i++) //NO SIEMPRE SE TRAE BLOQUE COMPLETO
      {
          if(i <  arrayInstrucciones.size())// arrayInstrucciones.size() cuando i sobrepasa el numero de elementos del array no saca nada
          {
               this.cacheDeInstrucciones[fila][columCache] = arrayInstrucciones.get(i);
               fila++;
          }
          else
          {
              this.cacheDeInstrucciones[fila][columCache] = -1;
              fila++;
          }
      }
      this.cacheDeInstrucciones[16][columCache] = bloque;
  }
  

private void recuperarDeCache(){
        int[] vecInstruccion = new int[4];
	int numBloc = this.hPC/16;
	int blocCache= numBloc % 8;
	int i= this.hPC-(numBloc*16);
	int inst=0;
        for(int j= i; j<i+4; j++){
            vecInstruccion[inst] = this.cacheDeInstrucciones[j][blocCache];
            inst++;
        }
	
        cambiarCiclo();
         this.hPC+=4;
	ejecutarInstruccion(vecInstruccion);
        if(this.comunicadores[numProcesador].terminado==true){
            limpiarRegistros();            
        }
        if(this.quantumNucleo > 0)
        {
            cambiarCiclo();
        }
        else
        {
            seAcaboQuantum();
            obtenerPC();
            if(comunicadores[0].contexto[33]==this.hPC){
                  cambiarRegistro(0);   
            }else{
                cambiarRegistro(1);
            }
            this.comunicadores[numProcesador].ocupado=true;
            
        }
	
}

private void seAcaboQuantum()
{
    contexto();
    this.comunicadores[numProcesador].ocupado=false;
    cambiarCiclo();
}
 
public void obtenerPC(){
     if(mainThread.hilos==1){
        PC=comunicadores[0].read();
       //this.pcFinal=comunicadores[0].getPcFinal();
        this.hPC=PC;
        comunicadores[1].hiloPC=-1;
         quantumNucleo = comunicadores[0].readQ();
    }else{
        if(comunicadores[numProcesador].terminado){
            this.PC = -1 ;
        }else{
            PC = comunicadores[numProcesador].read();
            this.hPC=PC;
            quantumNucleo = comunicadores[numProcesador].readQ();
             //this.pcFinal=comunicadores[numProcesador].getPcFinal();
       /* if(this.comunicadores[this.numProcesador].contexto[33]==hPC){
            int[] vectContexto = new int[34];
            vectContexto = this.comunicadores[this.numProcesador].contexto;
            cambiarRegistro(vectContexto);
        }*/
       }
     }
}


private void cambiarRegistro(int proc){
     for(int i = 0; i<33; i++){
         this.comunicadores[this.numProcesador].vectreg[i] = comunicadores[proc].contexto[i];
     }
}

private void limpiarRegistros(){
     for(int i = 0; i<33; i++){
         this.comunicadores[this.numProcesador].vectreg[i] = 0;
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
        while(i<2){ //mainThread.latencia
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
    if(this.comunicadores[this.numProcesador].seguir){
        obtenerPC();
        this.comunicadores[this.numProcesador].seguir=false;
      }
    cicloReloj++;
}

public void contexto()
    {
        for(int i = 0; i<33; i++)
        {
           this.comunicadores[this.numProcesador].contexto[i] = this.comunicadores[this.numProcesador].vectreg[i];
        }
        this.comunicadores[this.numProcesador].contexto[33] = this.hPC;
        limpiarRegistros();
        mainThread.vectPc.add(this.hPC);
        //mainThread.vectPcFinal.add(this.pcFinal);
    }

private void ejecutarInstruccion(int[] vector){
    System.out.println("Hilo " + this.numProcesador + ": leyendo instruccion con CP: " + this.hPC);
    int instruccion[] = new int[4];
        for(int i=0;i<4;i++){
        instruccion[i]=vector[i];
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
          imprimir();
        break;
      default:
        break;
    }    
}

 //hace una suma del valor del registro con un numero y lo guarda en un registro
  public void daddi(int regDestino, int regFuente, int numero){
     
    int valor = this.comunicadores[this.numProcesador].vectreg[regFuente]+numero;
    this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
    this.quantumNucleo--;
  }
  
  //Hace una suma de los valores de 2 registros y los guarda en un registro
  public void dadd(int regDestino, int regF1, int regF2){
    int valor = this.comunicadores[this.numProcesador].vectreg[regF1]+this.comunicadores[this.numProcesador].vectreg[regF2];
   this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
    this.quantumNucleo--;
  }
  
  //hace una resta de los valores de 2 registros y los guarda en un registro
  public void dsub(int regDestino, int regF1, int regF2){
      int valor = this.comunicadores[this.numProcesador].vectreg[regF1]-this.comunicadores[this.numProcesador].vectreg[regF2];
      this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
      this.quantumNucleo--;
  }
  
  //Si el valor es igual a 0 hace un salto
    public void beqz(int regComparacion, int salto){
         
        if(this.comunicadores[this.numProcesador].vectreg[regComparacion] == 0){
            this.hPC += salto*4;
        }
        this.quantumNucleo--;
    }
    
    //Si el valor del registro es diferente de 0 hace un salto
    public void bnez(int regComparacion, int salto){//segunda y cuarta parte, tercera vacia
       // System.out.println("valor "+registros[regComparacion]);
        if(this.comunicadores[this.numProcesador].vectreg[regComparacion] != 0){
            this.hPC += salto*4;
        }
        this.quantumNucleo--;
    }
    
    //
    public void jr(int regsalto){//segunda y cuarta parte, tercera vacia
        this.hPC =this.comunicadores[this.numProcesador].vectreg[regsalto];       
        this.quantumNucleo--;
    }
    
    //hace una multiplicacion de los valores de 2 registros y los guarda en un registro
    public void dmul(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = this.comunicadores[this.numProcesador].vectreg[regF1] * this.comunicadores[this.numProcesador].vectreg[regF2];
       this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
       this.quantumNucleo--;
        
    }
    
    //hace una division de los valores de 2 registros y los guarda en un registro
    public void ddiv(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = this.comunicadores[this.numProcesador].vectreg[regF1]/this.comunicadores[this.numProcesador].vectreg[regF2];
       this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
      this.quantumNucleo--;
    }
    
    //
    public void jal(int salto){//segunda y cuarta parte, tercera vacia
        this.comunicadores[this.numProcesador].vectreg[31]=this.hPC;
        this.hPC =this.hPC+salto;       
        this.quantumNucleo--;
    } 
    
   //Si el procesador llego al final del hilo, se desocupa e imprime los resultados
    public void fin(){
        this.comunicadores[numProcesador].ocupado = false;
        mainThread.vectPc.add(-1);
    }
    
    //imprime los resultados del hilo
    public void imprimirEstado(){
    	System.out.print("FIN de Hilo");
    }

  


}
