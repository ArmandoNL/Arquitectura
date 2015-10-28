import java.util.ArrayList;
import java.util.concurrent.*;

public class Nucleo implements Runnable {
    
 // private final int[] registros; 
  public Thread t;
  private final int[][] cacheDeInstrucciones; //donde se guarda la informacion de la cache.
  private final HiloControlador mainThread; //instancia del hilo controlador
  private final ArrayList<Integer> arrayInstrucciones; //contiene las instrucciones de memoria
  private final CyclicBarrier barrera; //se encarga de la sincronizacion de los procesadores
  private final int numProcesador; //id del procesade utilizado
  private int PC; //PC inicial de cada procesador
  private int pcFinal; //direccion donde termina el cada archivo
  private int hPC; //PC local de cada procesador
  private final Comunicador[] comunicadores; // instancia de comunicadores donde se guarda la informacion compartida
  private int quantumNucleo; //valor del quatum local
  boolean busOcupado; //funciona para mantener control del bus de cache
  boolean finalizar = false; //variable que funciona para terminar ka ejecucion del procesador.
  
  
  //nuevo constructor del procesador
  public Nucleo(HiloControlador hc, int id){	 //se inicializa con el hilo principal y el id del procesador
	 
          this.numProcesador = id; //id del procesador
          mainThread = hc; // instacia del hilo controlador
          arrayInstrucciones = mainThread.memTemp;  //instancia de la memoria de instrucciones 
          barrera = mainThread.barrier;  
          comunicadores = mainThread.comunicadores;          
          this.cacheDeInstrucciones = new int[17][8];  //se inicializa el cache de instrucciones con -1 en el id de bloque
	  for(int i = 0; i < 8; i++){                
            this.cacheDeInstrucciones[16][i] = -1;
	  }	
  }
  
  
  /*  Se encarga de ejecutar los procesadores*/
  @Override
  public void run(){
    obtenerPC();
    while(!this.comunicadores[this.numProcesador].terminado){
        if(estaenCache(hPC)){
            ejecutarDeCache();
        }else{
            falloCache();
        }
    }     
   seTermino();
   System.out.println("Se termino");
   
}
 
  //se encarga de verificar que ambos procesadores hayan termiando, si no se mantiene esperando en la barrera.
  private void seTermino(){
        while(!finalizar){
        cambiarCiclo();
        if(comunicadores[0].hiloPC==-1 && comunicadores[1].hiloPC==-1){
             finalizar=true;
        }
    } 
  }
  
  /*
      Efecto: Revisa si el bloque que se necesita esta en cache
      Requiere: Un int 
      Modifica: Un boleano que indica si esta o no el bloque
    */
  public boolean estaenCache(int hpc){
       int bloque = hpc/16;
       int columCache = bloque%8;
      return this.cacheDeInstrucciones[16][columCache] == bloque; 
  }
  
  
   /*
      Efecto: Recupera el bloque de memoria
      Requiere: el PC de la instruccion que se busca
      Modifica: La cache de instrucciones con el bloque deseado
    */
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
  
/*    Efecto: Ejecuta la instruccion segun el PC 
      Requiere: que el bloque de instrucciones se encuentre en la cache de instrucciones
      Modifica: los registros de las instrucciones
    */
private void  ejecutarDeCache(){
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
	ejecutarInstruccion(vecInstruccion); //se encarga de ejecutar cada instrucciones en el cache
        cambiarCiclo();

        if(this.quantumNucleo == 0) //si el quantum se gasta se guarda el contexto
        { 
            seAcaboQuantum();
        }
	
}

/*    Efecto: guarda el contexto de los registros  y el PC si se acaba el quantum
      Requiere: que se haya acabado el quantum
      Modifica: el contexto y los registros
    */
private void seAcaboQuantum() 
{
    contexto();  //guarda el contexto de los registros y el pc en un vector temporal
    limpiarRegistros();
    this.comunicadores[numProcesador].ocupado=false;
    cambiarCiclo();  //una vez que se guarda el contexto, se asigna un nuevo PC.
   
    
}
 
   /* Efecto: Obtiene el PC inicial de cada procesador
      Modifica: el valor del primer PC
    */
public void obtenerPC(){
     if(mainThread.hilos==1){
        PC=comunicadores[0].read();
        this.pcFinal=comunicadores[0].getPcFinal();
        quantumNucleo = comunicadores[0].readQ();
        this.hPC=PC;
        comunicadores[1].hiloPC=-1;
        comunicadores[1].cambiarCiclo = true;
    }else{      
        pcSiguiente();
       }
    }


/*  Efecto: transfiere los valores de los registros que estan guardados en un contexto
    Requiere: el numero del procesador donde se encuentra el contexto
    Modifica: los registros del procesador
    */
private void cambiarRegistro(int proc){
    int[] vecTemp = this.comunicadores[proc].pedirContexto();
      for(int i =0; i<33;i++){
          this.comunicadores[this.numProcesador].vectreg[i] = vecTemp[i];
      }
}

/*  Efecto: limpias los registros del procesador que lo pide.
    Requiere: saber el numero del procesador que lo pide.
    Modifica: los valores de los registros.
    */
private void limpiarRegistros(){
     for(int i = 0; i<34; i++){
         this.comunicadores[this.numProcesador].vectreg[i] = 0;
     }
}


/*  Efecto: se encarga de obtener el bus devolviendo true, si no devuelve falso.
    Requiere: que un procesador pida el bus.
    Modifica: el valor del bus.
    */
boolean pedirBus(){ 
    if(this.comunicadores[this.numProcesador].semaforoCache.tryAcquire()){
        busOcupado = true;
        return true;
    }else{
        return false;
    }
}

/*  Efecto: se encarga de soltar el semaforo del bus.
    Requiere: que un procesador pida soltar el bus.
    Modifica: el valor del bus.
    */
boolean liberarBus(){ //libera el bus una vez que no se necesita.
    this.comunicadores[this.numProcesador].semaforoCache.release();
    busOcupado = false;
    return true;
}

/*  Efecto: si bloque no se encuentra en cache, solicita el bus para recuperar el bloque de memoria.
    Requiere: que el bloque solicitado no se encuentre en cache.
    Modifica: el cache de instrucciones
    */
private void falloCache(){ 
    if(pedirBus()){
        traerBloque();
        int i=0;
       while(i<mainThread.latencia){ //se encarga de cambiar de ciclo segun el valor de m y b ingresado por el usuario.
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

/*  Efecto: se encarga sincronizar los procesadores a la hora de pedir cambio de ciclo de reloj.
    Requiere: cambiar de ciclo de reloj.
    Modifica: el valor del ciclo del reloj.
    */
private void cambiarCiclo(){
    this.comunicadores[this.numProcesador].cambiarCiclo = true; //avisa que esta listo para cambiar ciclo
   
    try{     
        barrera.await();
            this.miEstado();
        
    }catch (InterruptedException | BrokenBarrierException e){}
    if(this.comunicadores[this.numProcesador].seguir){ //si hay mas PCs para entregar se pide el pc.
        pcSiguiente();
        this.comunicadores[this.numProcesador].seguir=false;
    }
}


/*  Efecto: se encarga de obtener el PC siguiente y si este tiene un contexto lo solicita.
    Requiere: que no se hayan acabado los PCs
    Modifica: el PC local y el los registros segun el contexto, si tienen.
    */
private void pcSiguiente(){
    this.PC = comunicadores[numProcesador].read();
    this.hPC=PC;
    this.quantumNucleo = comunicadores[numProcesador].readQ();
    this.pcFinal=comunicadores[numProcesador].getPcFinal();
    
    if(comunicadores[0].contextos.size()>0)
    {
        if(comunicadores[0].contextos.get(0)[33] ==this.hPC){
                  cambiarRegistro(0); 
        }
    }
    if(comunicadores[1].contextos.size()>0)
    {
        if(comunicadores[1].contextos.get(0)[33]==this.hPC){
            cambiarRegistro(1);
        }
    }
    this.comunicadores[numProcesador].ocupado=true;
}

/*  Efecto: guarda el contexto de los registros cuando se ha acabado el PC y agrega el PC a la cola de PCs
    Requiere: que se haya acabado el quantum
    Modifica: el contexto y la cola de PCs
    */
public void contexto()
    {
        int[] vec = new int[34];
        for(int i = 0; i<33; i++)// guarda en cada posicion del contexto el valor del registro.
        {
           vec[i] = this.comunicadores[this.numProcesador].vectreg[i];
        }
        vec[33] = this.hPC; //en la posicion 33 del contexto guarda el PC
        this.comunicadores[this.numProcesador].guardarContexto(vec);
        System.out.println("Valor de PC " + this.hPC);
        mainThread.vectPc.add(this.hPC);
        System.out.println("METO A COLA:" + mainThread.vectPc.peek());
        mainThread.vectPcFinal.add(this.pcFinal);
    }

 /*
      Efecto: Lee las instrucciones parseadas y 
      Requiere: Un vectos de ints que sera la instruccion a procesar 
      Modifica: Nada 
    */
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

 /*
      Efecto: Realiza una suma y la guarda en el registro de destino
      Requiere: Dos int que se sumaran y un int de destino 
      Modifica: el quantum y el registro de destino  
    */
  public void daddi(int regDestino, int regFuente, int numero){
     
    int valor = this.comunicadores[this.numProcesador].vectreg[regFuente]+numero;
    this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
    this.quantumNucleo--;
  }
  
  /*
      Efecto: Realiza una suma y la guarda en el registro de destino
      Requiere: Dos int que se sumaran y un int de destino 
      Modifica: el quantum y el registro de destino  
    */
  public void dadd(int regDestino, int regF1, int regF2){
    int valor = this.comunicadores[this.numProcesador].vectreg[regF1]+this.comunicadores[this.numProcesador].vectreg[regF2];
   this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
    this.quantumNucleo--;
  }
  
  /*
      Efecto: Realiza una resta y la guarda en el registro de destino
      Requiere: Dos int que se restaran y un int de destino 
      Modifica: el quantum y el registro de destino  
    */
  public void dsub(int regDestino, int regF1, int regF2){
      int valor = this.comunicadores[this.numProcesador].vectreg[regF1]-this.comunicadores[this.numProcesador].vectreg[regF2];
      this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
      this.quantumNucleo--;
  }
  
   /*
      Efecto: Realiza un salto si el valor en el registro es igual a cero
      Requiere: Dos int
      Modifica: El quantum y el hPC 
    */
    public void beqz(int regComparacion, int salto){
         
        if(this.comunicadores[this.numProcesador].vectreg[regComparacion] == 0){
            this.hPC += salto*4;
        }
        this.quantumNucleo--;
    }
    
     /*
      Efecto: Realiza un salto si el valor del registro es diferente de cero
      Requiere: Dos int 
      Modifica: El quantum y el hPC 
    */
    public void bnez(int regComparacion, int salto){//segunda y cuarta parte, tercera vacia
       // System.out.println("valor "+registros[regComparacion]);
        if(this.comunicadores[this.numProcesador].vectreg[regComparacion] != 0){
            this.hPC += salto*4;
        }
        this.quantumNucleo--;
    }
    
     /*
      Efecto: Realiza un salto
      Requiere: Un int
      Modifica: El hPC y el quantum 
    */
    public void jr(int regsalto){//segunda y cuarta parte, tercera vacia
        this.hPC =this.comunicadores[this.numProcesador].vectreg[regsalto];       
        this.quantumNucleo--;
    }
    
    /*
      Efecto: Realiza una multiplicacion y la guarda en el registro de destino
      Requiere: Dos int que se multiplicaran y un int de destino 
      Modifica: el quantum y el registro de destino  
    */
    public void dmul(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = this.comunicadores[this.numProcesador].vectreg[regF1] * this.comunicadores[this.numProcesador].vectreg[regF2];
       this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
       this.quantumNucleo--;
        
    }
        
     /*
      Efecto: Realiza una division y la guarda en el registro de destino
      Requiere: Dos int que se dividiran y un int de destino 
      Modifica: el quantum y el registro de destino  
    */
    public void ddiv(int regDestino, int regF1, int regF2){//segunda y cuarta parte, tercera vacia
       int valor = this.comunicadores[this.numProcesador].vectreg[regF1]/this.comunicadores[this.numProcesador].vectreg[regF2];
       this.comunicadores[this.numProcesador].vectreg[regDestino]= valor;
      this.quantumNucleo--;
    }
    
     /*
      Efecto: Adelanta el hPC segun el int que le entra
      Requiere: Un int  
      Modifica: El quantum, el regustro 31 y el hpc  
    */
    public void jal(int salto){
        this.comunicadores[this.numProcesador].vectreg[31]=this.hPC;
        this.hPC =this.hPC+salto;       
        this.quantumNucleo--;
    } 
    
     /*
      Efecto: Instruccion que coloca la variable ocupado en falso 
      Requiere: Nada 
      Modifica: La variable ocupado de la clase comunicador
    */
    public void fin(){
        this.comunicadores[numProcesador].ocupado = false;
    }
    
     /*
      Efecto: Envia los resultados que se necesitan imprimir cuando termina cada hilo
      Requiere: Nada 
      Modifica: Nada 
    */
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
          //System.out.println("Num Procesador" + this.numProcesador);
          String text="Valor de Registros del archivo: "+ numero + "\n";          
          for(int i =0; i<34; i++){
               text+=" Reg: " + i + " =" + comunicadores[this.numProcesador].pedirCampoRegistro(i)+", ";             
           }
          text+="\n";
          text+="El quantum es :" + this.quantumNucleo;
          text+="\n";
          text+="El reloj es :" + mainThread.ciclosReloj;
          text+="\n";
          //text+="Contador Interno de programa Actual(hPC) :" + hPC;
          //text+="\n";
          text+= "Numero de Procesador que corrio el archivo : " + this.numProcesador;
          /*text+="Campo del vector :" +numero2 ;*/
          text+="\n\n";
         
           mainThread.imprimirPantalla(text);       	
    }
    
    public void miEstado(){ 
        int num=0;
        for(int i=1; i<= mainThread.contArchivos*2; i=i+2){
            
            if(this.hPC<=mainThread.nombreArchivo[i]){ //se ocupa otra condicion porque 0 siempre va a ser menor que todos los demas.
                //tiene que ser algo como >=PC donde empieza && <pc donde termina.
                num=mainThread.nombreArchivo[i-1];
            }
        }
        String text=""+ num; 
        if(this.numProcesador==0){
           mainThread.imprimirEstado0(text);
        }else if(this.numProcesador==1){
            mainThread.imprimirEstado1(text);
        }
     }
    
    
}
