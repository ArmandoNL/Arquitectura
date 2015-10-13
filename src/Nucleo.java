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
       
   seTermino();
   
   System.out.println("se terminooo");
}
 
  private void seTermino(){
        while(!finalizar){
        cambiarCiclo();
        if(comunicadores[0].hiloPC==-1 && comunicadores[1].hiloPC==-1){
             finalizar=true;
        }
    } 
  }
  public boolean estaenCache(int hpc){
       int bloque = hpc/16;
       int columCache = bloque%8;
      return this.cacheDeInstrucciones[16][columCache] == bloque; 
  }
  
 /* public void imprimir(){
        System.out.println("SOY HILO " + this.numProcesador);
         System.out.println("El quantum es :" + this.quantumNucleo);
          System.out.println("El reloj es :" + cicloReloj);
          for(int i =0; i<33; i++){
               System.out.println("Reg" + i + "=" + comunicadores[this.numProcesador].pedirCampoRegistro(i));             
           }
  }*/
  
  public void traerBloque()
  {
      int bloque = this.hPC/16;
      int j = (bloque*16)+16;
      int columCache = bloque%8;
      int fila = 0;
      for(int i=bloque*16;i<j;i++) //NO SIEMPRE SE TRAE BLOQUE COMPLETO
      {
          if(i < this.pcFinal)// arrayInstrucciones.size() cuando i sobrepasa el numero de elementos del array no saca nada
          {
               this.cacheDeInstrucciones[fila][columCache] = arrayInstrucciones.get(i);
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
        this.hPC+=4;
	ejecutarInstruccion(vecInstruccion);
        cambiarCiclo();
        
        
        /*if(this.quantumNucleo > 0)
        {
            cambiarCiclo();
        }*/
        if(this.quantumNucleo == 0)
        {
            seAcaboQuantum();
           // obtenerPC();
          /*  if(comunicadores[0].contexto[33]==this.hPC){
                  cambiarRegistro(0);   
            }else if(comunicadores[1].contexto[33]==this.hPC){
                cambiarRegistro(1);
            }
            this.comunicadores[numProcesador].ocupado=true;*/
        }
	
}

private void seAcaboQuantum() //cuando el quatum es igual a 0
{
    contexto();  //guarda el contexto de los registros y el pc en un vector temporal
    limpiarRegistros();
    this.comunicadores[numProcesador].ocupado=false;
    cambiarCiclo();
   
    
}
 
public void obtenerPC(){
     if(mainThread.hilos==1){
        PC=comunicadores[0].read();
        this.pcFinal=comunicadores[0].getPcFinal();
        this.hPC=PC;
        comunicadores[1].hiloPC=-1;
        comunicadores[1].cambiarCiclo = true;
        quantumNucleo = comunicadores[0].readQ();
    }else{      
        pcSiguiente();
       }
    }


private void cambiarRegistro(int proc){
    int[] vecTemp = comunicadores[proc].pedirContexto();
      for(int i =0; i<33; i++){
          comunicadores[this.numProcesador].vectreg[i] = vecTemp[i];
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
    
    if(pedirBus()){
        traerBloque();
        int i=0;
        while(i<mainThread.latencia){ //mainThread.latencia
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
    this.comunicadores[this.numProcesador].cambiarCiclo = true; //avisa que esta listo para cambiar ciclo
    
    try{
        barrera.await();
    }catch (InterruptedException | BrokenBarrierException e){}
    
    if(this.comunicadores[this.numProcesador].seguir){
        pcSiguiente();
        this.comunicadores[this.numProcesador].seguir=false;
    }
    //cicloReloj++;
}

private void pcSiguiente(){
    this.PC = comunicadores[numProcesador].read();
    this.hPC=PC;
    this.quantumNucleo = comunicadores[numProcesador].readQ();
    this.pcFinal=comunicadores[numProcesador].getPcFinal();
    
    if(comunicadores[0].contextos.size()>0){
    
        if(comunicadores[0].contextos.get(0)[33] ==this.hPC){
            cambiarRegistro(0);   
        }
    }
    if(comunicadores[1].contextos.size()>0){
        if(comunicadores[1].contextos.get(0)[33]==this.hPC){
            cambiarRegistro(1);   
        }
    }
    
    this.comunicadores[numProcesador].ocupado=true;
}

public void contexto()
    {
        int[] vec = new int[34];
        for(int i = 0; i<33; i++)// guarda en cada posicion del contexto el valor del registro.
        {
           vec[i] = this.comunicadores[this.numProcesador].vectreg[i];
        }
        vec[33] = this.hPC; //en la posicion 33 del contexto guarda el PC
        this.comunicadores[this.numProcesador].guardarContexto(vec);
        limpiarRegistros();
        mainThread.vectPc.add(this.hPC);
        mainThread.vectPcFinal.add(this.pcFinal);
    }

private void ejecutarInstruccion(int[] vector){
    //System.out.println("Hilo " + this.numProcesador + ": leyendo instruccion con CP: " + this.hPC);
    int instruccion[] = new int[4];
        for(int i=0;i<4;i++){
        instruccion[i]=vector[i];
        }    
   
   //System.out.println("Se leyo instruiccion: " +instruccion[0]+" " +instruccion[1]+ " " +instruccion[2]+" " +instruccion[3]);
 
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
          imprimirEstado();
          limpiarRegistros(); 
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
    }
    
    //imprime los resultados del hilo
    public void imprimirEstado(){ 
        int numero=0;
        //String numero2="";
        
        for(int i=1; i< mainThread.contArchivos*2; i=i+2){
            if(hPC==mainThread.nombreArchivo[i]){
                numero=mainThread.nombreArchivo[i-1];
            }
           // numero2=mainThread.nombreArchivo[i];
        } 
       /* for(int j=0; j< mainThread.nombreArchivo.length; j++){
            
            numero2+=mainThread.nombreArchivo[j];
        }*/
          System.out.println("Num Procesador" + this.numProcesador);
          String text="Valor de Registros del archivo: "+ numero + "\n";          
          for(int i =0; i<34; i++){
               text+=" Reg: " + i + " =" + comunicadores[this.numProcesador].pedirCampoRegistro(i)+", ";             
           }
          text+="\n";
          text+="El quantum es :" + this.quantumNucleo;
          text+="\n";
          text+="El reloj es :" + mainThread.ciclosReloj;
          text+="\n";
          text+="hPC :" + hPC;
          text+="\n";
          text+= "Num Procesador" + this.numProcesador;
          /*text+="Campo del vector :" +numero2 ;*/
          text+="\n\n";
         
           mainThread.imprimirPantalla(text);       	
    }
}
