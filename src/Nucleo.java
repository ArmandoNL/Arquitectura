import java.util.ArrayList;
import java.util.concurrent.*;

public class Nucleo implements Runnable {
    
 // private final int[] registros; 
  public Thread t;
  private final int[][] cacheDeInstrucciones; //donde se guarda la informacion de la cache.
  public final int[][] cacheDeDatos;//cache de datos
  private final HiloControlador mainThread; //instancia del hilo controlador
  private final ArrayList<Integer> memInstrucciones; //memoria de instrucciones
  private final int[] memDatos; //memoria de datos
  public final char[] estadoCacheDatos; //memoria de datos
  private final CyclicBarrier barrera; //se encarga de la sincronizacion de los procesadores
  private final int numProcesador; //id del procesade utilizado
  private int PC; //PC inicial de cada procesador
  private int pcFinal; //direccion donde termina el cada archivo
  private int hPC; //PC local de cada procesador
  private final Comunicador[] comunicadores; // instancia de comunicadores donde se guarda la informacion compartida
  private int quantumNucleo; //valor del quatum local
  boolean busOcupado; //funciona para mantener control del bus de cache
  boolean finalizar = false; //variable que funciona para terminar ka ejecucion del procesador.
  public int otraCache;
  private boolean instCompletada = true; 
  public boolean soyLoadLink;
  
  
  //nuevo constructor del procesador
  public Nucleo(HiloControlador hc, int id){	 //se inicializa con el hilo principal y el id del procesador
	 
          this.numProcesador = id; //id del procesador
          mainThread = hc; // instacia del hilo controlador
          memInstrucciones = mainThread.memTemp;  //instancia de la memoria de instrucciones 
          memDatos = mainThread.memDatos;
          barrera = mainThread.barrier;  
          comunicadores = mainThread.comunicadores;          
          this.cacheDeInstrucciones = new int[17][8];  //se inicializa el cache de instrucciones con -1 en el id de bloque
	  for(int i = 0; i < 8; i++){                
            this.cacheDeInstrucciones[16][i] = -1;
	  }	
          this.cacheDeDatos = new int[5][8];
          for(int i = 0; i < 8; i++){                
            this.cacheDeDatos[4][i] = -1;
	  }
          estadoCacheDatos = new char[8];
          for(int i=0;i<7;i++){
              this.estadoCacheDatos[i] = 'I';
          }
          if(this.numProcesador==0){
              this.otraCache = 1;
          }
          else{
              this.otraCache = 0;
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
          if(i < this.pcFinal)// memInstrucciones.size() cuando i sobrepasa el numero de elementos del array no saca nada
          {
               this.cacheDeInstrucciones[fila][columCache] = memInstrucciones.get(i);
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
        
        while(!instCompletada){ //si la instruccion no se ha completado tiene que volver a ejecutarla hasta que se complete.
            instCompletada=true;
            ejecutarInstruccion(vecInstruccion); //se encarga de ejecutar cada instrucciones en el cache
            cambiarCiclo();
        }
        
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
    if(this.comunicadores[this.numProcesador].busCacheInst.tryAcquire()){
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
    this.comunicadores[this.numProcesador].busCacheInst.release();
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
        this.miEstado();
        barrera.await();
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
        for(int i = 0; i<32; i++)// guarda en cada posicion del contexto el valor del registro.
        {
           vec[i] = this.comunicadores[this.numProcesador].vectreg[i];
        }
        vec[32]= -1; //pongo -1 en RL
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
       case 35:             
          lw(instruccion[1],instruccion[2],instruccion[3]);
        break;
      case 43:             
          sw(instruccion[1],instruccion[2],instruccion[3]);
        break;
      case 50:             
          ll(instruccion[2],instruccion[3]);
        break;
      case 51:             
          sc(instruccion[2],instruccion[3]);
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
        
        for(int i=1; i< mainThread.contArchivos*3; i=i+3){
            if(hPC==mainThread.nombreArchivo[i]){
                numero=mainThread.nombreArchivo[i-1];
            }
            //numero2=mainThread.nombreArchivo[i];
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
        //  text+="Campo del vector :" +numero2 ;
          text+="\n\n";
         
           mainThread.imprimirPantalla(text);
    }
    
    public void miEstado(){ 
        int num=0;
        String text="";
        for(int i=1; i<= mainThread.contArchivos*3; i=i+3){
            
            if(this.hPC<mainThread.nombreArchivo[i] && this.hPC>=mainThread.nombreArchivo[i+1]){ //se ocupa otra condicion porque 0 siempre va a ser menor que todos los demas.
                //tiene que ser algo c omo >=PC donde empieza && <pc donde termina.
                num=mainThread.nombreArchivo[i-1];                
               // System.out.println("Mayor o igual a  " + mainThread.nombreArchivo[i+1]);
              //  System.out.println("Menor a " + mainThread.nombreArchivo[i]);
                
            }
        }
        
        
        if(num!=-1){
            text+= num; 
        }else{
            text+= "Pr"; 
        }
        if(this.numProcesador==0){
           mainThread.imprimirEstado0(text);
        }else if(this.numProcesador==1){
            mainThread.imprimirEstado1(text);
        }
     }
    
    public void lw(int regSum, int regLectura,int dirMem){
        int dato = 0;
         int i=0;
         boolean esta;
         int numBloque = (this.comunicadores[this.numProcesador].vectreg[regSum]+dirMem)/16;
         int posCache = numBloque%8;
        
         if(!pedirMiCache()){//Mato el hilo
             instCompletada=false;         
         }else{             
                 if(cacheDeDatos[4][posCache]==numBloque){
                     esta=true;
                 }else{//fallo
                     esta=false;
                 }
                int palabra = ((this.comunicadores[this.numProcesador].vectreg[regSum] + dirMem)%16)/4;
            
             if(esta){
             
                char estado = estadoCacheDatos[posCache];             

                 switch(estado){
                     case('C'): 
                        dato=cacheDeDatos[palabra][posCache];            
                        comunicadores[this.numProcesador].vectreg[regLectura]=dato;
                        this.quantumNucleo--;
                        if(soyLoadLink){
                            mainThread.llActivo[0]= 1 ;
                            mainThread.llActivo[1]= this.numProcesador ;
                            mainThread.llActivo[2]= posCache ;
                        }
                         break;
                     case('M'):
                        dato=cacheDeDatos[palabra][posCache];            
                        comunicadores[this.numProcesador].vectreg[regLectura]=dato;
                        this.quantumNucleo--;
                         if(soyLoadLink){
                            mainThread.llActivo[0]= 1 ;
                            mainThread.llActivo[1]= this.numProcesador ;
                            mainThread.llActivo[2]= posCache ;
                        } 
                         break;
                     case('I'):   //esta invalido en MI cache                      
                             if(pedirBusDatos()){
                                 while(!pedirOtraCache()){
                                     cambiarCiclo(); //se encicla si no
                                 } 
                                 int posMem = ((dirMem+regSum)%640)/4;
                                 // int posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                                     if(mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache]=='M'){
                                         
                                         for(int x=0; x<4; x++){
                                             this.cacheDeDatos[x][posCache]=mainThread.nucleos[this.otraCache].cacheDeDatos[x][posCache];
                                             memDatos[posMem+x]=mainThread.nucleos[this.otraCache].cacheDeDatos[x][posCache];                                   
                                         }
                                         mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache]='C';
                                     }else{
                                       for(int x=0; x<4; x++){
                                         this.cacheDeDatos[x][posCache]=memDatos[posMem+x];                                      
                                       }
                                     }
                                     
                                    while(i<1){//mainThread.latencia
                                        cambiarCiclo();
                                        i++;
                                    }                                   
                                    liberarOtraCache();
                                    liberarBusDatos();
                                    this.estadoCacheDatos[posCache]='C';
                                    dato=cacheDeDatos[palabra][posCache];            
                                    comunicadores[this.numProcesador].vectreg[regLectura]=dato;
                                     this.quantumNucleo--;
                                    if(soyLoadLink){
                                         mainThread.llActivo[0]= 1 ;
                                         mainThread.llActivo[1]= this.numProcesador ;
                                         mainThread.llActivo[2]= posCache ;
                                    }
                             }else{
                                 instCompletada=false;
                             }                        
                          break;                     
                     default:
                         break;                        
                    
                }                 
             }else{ //no esta en mi cache
                   if(pedirBusDatos()){
                         while(!pedirOtraCache()){
                            cambiarCiclo();
                         } 
                         if(mainThread.nucleos[this.otraCache].cacheDeDatos[4][posCache]==numBloque){//Si esta en la otra cache
                         
                           int posMem = ((dirMem+regSum)%640)/4;            
                           //int posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4; 
                           if(mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache]=='M'){
                               if(this.estadoCacheDatos[posCache]=='M'){ //bloque en mi cache esta en M. Lo bajo primero.
                                   int posMem2=((dirMem+regSum)%640)/4;
                                   //int posMem2 = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                                   for(int y=0; y<4; y++){
                                        memDatos[posMem2+y]=this.cacheDeDatos[y][posCache]; //bajo bloque de memoria en mi cache.
                                   }
                               }
                               //si no esta en M el mio. Bajo bloque a memoria y lo copio en mi cache.
                               for(int x=0; x<4; x++){ //si mi blqoue no esta en M copio de la cache.
                                    this.cacheDeDatos[x][posCache]=mainThread.nucleos[this.otraCache].cacheDeDatos[x][posCache];
                                    memDatos[posMem+x]=mainThread.nucleos[this.otraCache].cacheDeDatos[x][posCache];
                               }
                               
                               mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache]='C';
        
                            }else{ //si no esta en M en la otra cache traigo de memoria
                                for(int x=0; x<4; x++){
                                    this. cacheDeDatos[x][posCache]=memDatos[posMem+x];                                      
                                }
                                //this.estadoCacheDatos[posCache]='C';
                                this.cacheDeDatos[4][posCache]=numBloque; //poner num de bloque cuando se sube nuevo bloque
                            }
                            while(i<1){ //mainThread.latencia
                                cambiarCiclo();
                                i++;
                            }                            
                            liberarOtraCache();
                            liberarBusDatos();
                            
                            
                         }else{// Si no esta del todo
                            int posMem3 = ((dirMem+regSum)%640)/4;
                            //int posMem3 = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                            for(int z=0; z<4; z++){
                                this.cacheDeDatos[z][posCache]=memDatos[posMem3+z];                                      
                            }
                            liberarOtraCache();
                            liberarBusDatos();
                         }
                         this.cacheDeDatos[4][posCache]=numBloque; //poner num de bloque cuando se sube nuevo bloque
                         estadoCacheDatos[posCache]='C';
                         dato=cacheDeDatos[palabra][posCache];            
                         comunicadores[this.numProcesador].vectreg[regLectura]=dato;
                         this.quantumNucleo--;
                         if(soyLoadLink){
                            mainThread.llActivo[0]= 1 ;
                           mainThread.llActivo[1]= this.numProcesador ;
                            mainThread.llActivo[2]= posCache ;
                        } 
                    }else{
                        instCompletada=false;
                    } 
             } 
         }
         liberarMiCache();
    }
    
    public void sw(int regSum, int regDato,int dirMem){        
        int numBloque = (this.comunicadores[this.numProcesador].vectreg[regSum] + dirMem)/16;
        int posCache = numBloque%8;
        int dato = this.comunicadores[this.numProcesador].vectreg[regDato];
        
        if(!pedirMiCache()){
            instCompletada = false;
        }
        else
        {
            int posMem = ((dirMem+regSum)%640)/4; //mapeo de dir de memoria a nuestro vect de memoria
            char estado = estadoCacheDatos[posCache];
            int palabra = ((this.comunicadores[this.numProcesador].vectreg[regSum] + dirMem)%16)/4;
            if(this.cacheDeDatos[4][posCache] == numBloque){//si el bloque esta en mi cache
                switch(estado){
                    case('M'):
                        this.cacheDeDatos[palabra][posCache] = dato;
                        liberarMiCache();
                        break;
                    case('C'):
                        if(pedirBusDatos()){
                            this.cacheDeDatos[palabra][posCache] = dato;
                            this.estadoCacheDatos[posCache] = 'M';
                            invalidar(posCache);
                            liberarBusDatos();//lo liberamos aqui o que el padre lo libere.
                            liberarMiCache();
                        }
                        else{
                            instCompletada = false;
                        }
                        break;
                    case('I'):
                        if(pedirBusDatos()){
                            while(!pedirOtraCache()){
                                cambiarCiclo();
                            }//mientras no este deiponible la otra cache, esperamos.
                            if(mainThread.nucleos[this.otraCache].cacheDeDatos[4][posCache] == numBloque){//si el bloque está en la otra caché
                                char estadoOtraCache = mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache]; //estado de otra caché
                                int i = 0;
                                switch(estadoOtraCache){
                                    case('M'):
                                        for(int j=0;j<4;j++){
                                           memDatos[posMem+j] = mainThread.nucleos[this.otraCache].cacheDeDatos[j][posCache];
                                           this.cacheDeDatos[j][posCache] = mainThread.nucleos[this.otraCache].cacheDeDatos[j][posCache];
                                        }
                                        mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache] = 'I';
                                        this.cacheDeDatos[4][posCache]=numBloque;
                                        this.cacheDeDatos[palabra][posCache] = dato;
                                        this.estadoCacheDatos[posCache] = 'M';
                                        while(i<1){//<mainThread.latencia
                                            cambiarCiclo();
                                            i++;
                                        }
                                        liberarOtraCache();
                                        liberarBusDatos();
                                        liberarMiCache();
                                        break;
                                    case('I'): //esta en mi caché y en la otra invalido
                                        liberarOtraCache();
                                        for(int j=0;j<4;j++){ //se trae directo de memoria
                                           this.cacheDeDatos[j][posCache] =  memDatos[posMem+j];
                                        }
                                        this.cacheDeDatos[4][posCache]=numBloque;
                                        this.cacheDeDatos[palabra][posCache] = dato;
                                        this.estadoCacheDatos[posCache] = 'M';
                                        while(i<1){ //mainThread.latencia
                                            cambiarCiclo();
                                            i++;
                                        }
                                        liberarBusDatos();
                                        liberarMiCache();
                                        break;
                                    case('C'):
                                        for(int j=0;j<4;j++){ //se trae directo de meroia
                                           this.cacheDeDatos[j][posCache] =  memDatos[posMem+j];
                                        }
                                        this.cacheDeDatos[4][posCache]=numBloque;
                                        this.cacheDeDatos[palabra][posCache] = dato;
                                        this.estadoCacheDatos[posCache] = 'M';
                                        mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache] = 'I';
                                        while(i<1){ //mainThread.latencia
                                            cambiarCiclo();
                                            i++;
                                        }
                                        liberarOtraCache();
                                        liberarBusDatos();
                                        liberarMiCache();
                                        break;
                                }
                            }
                            else{ //si está en mi caché pero NO en la otra.
                                liberarOtraCache(); //se trae directo de memoria
                                for(int j=0;j<4;j++){
                                    this.cacheDeDatos[j][posCache] =  memDatos[posMem+j];
                                }    
                                this.cacheDeDatos[4][posCache]=numBloque;
                                this.cacheDeDatos[palabra][posCache] = dato;
                                this.estadoCacheDatos[posCache] = 'M';
                                liberarBusDatos();
                                liberarMiCache();
                            }
                        }
                        else{
                            instCompletada = false;
                        }
                        break;
                    default: break;
                }
            }
            else
            {//cuando no está el valor en mi cache
                if(this.estadoCacheDatos[posCache]=='M'){ //como el bloque no esta hay que fijarse si en ESE bloque hay uno en M para bajarlo a memoria primero.
                    posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                    for(int j=0;j<4;j++){
                        memDatos[posMem+j] = this.cacheDeDatos[j][posCache];
                    }
                    int i=0;
                    while(i<1){ //mainThread.latencia
                        cambiarCiclo();
                        i++;
                    }
                }  
                if(pedirBusDatos()){ //para ir a buscar en la otra caché
                    while(!pedirOtraCache()){
                        cambiarCiclo();
                    }//mientras no este deiponible la otra cache, esperamos.
                    if(mainThread.nucleos[this.otraCache].cacheDeDatos[4][posCache] == numBloque){//si esta en la otra cache
                        char estadoOtraCache = mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache];
                        int i = 0;
                        switch(estadoOtraCache){
                            case('M'):
                                for(int j=0;j<4;j++){
                                    memDatos[posMem+j] = mainThread.nucleos[this.otraCache].cacheDeDatos[j][posCache];
                                }
                                /*if(this.estadoCacheDatos[posCache]=='M'){
                                    posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                                    for(int j=0;j<4;j++){
                                        memDatos[posMem+j] = this.cacheDeDatos[j][posCache];
                                    }
                                
                                    while(i<mainThread.latencia){ 
                                        cambiarCiclo();
                                        i++;
                                    }
                                }*/
                                for(int j=0;j<4;j++){
                                    this.cacheDeDatos[j][posCache] = mainThread.nucleos[this.otraCache].cacheDeDatos[j][posCache];
                                }
                                this.cacheDeDatos[4][posCache]=numBloque;
                                mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache] = 'I';
                                this.cacheDeDatos[palabra][posCache] = dato;
                                this.estadoCacheDatos[posCache] = 'M';
                                while(i<1){ //mainThread.latencia
                                    cambiarCiclo();
                                    i++;
                                }
                                liberarOtraCache();
                                liberarBusDatos();
                                liberarMiCache();
                                break;
                            case('I'): //no en mi caché y en la otra invalido
                                liberarOtraCache();
                                /*if(this.estadoCacheDatos[posCache]=='M'){
                                    posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                                    for(int j=0;j<4;j++){
                                        memDatos[posMem+j] = this.cacheDeDatos[j][posCache];
                                    }
                                }*/
                                for(int j=0;j<4;j++){
                                    this.cacheDeDatos[j][posCache] =  memDatos[posMem+j];   
                                }
                                this.cacheDeDatos[4][posCache]=numBloque;
                                this.cacheDeDatos[palabra][posCache] = dato;
                                this.estadoCacheDatos[posCache] = 'M';
                                while(i<1){  //mainThread.latencia
                                    cambiarCiclo();
                                    i++;
                                }
                                liberarBusDatos();
                                liberarMiCache();
                                break;
                            case('C'):
                                /*if(this.estadoCacheDatos[posCache]=='M'){
                                    posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4;
                                    for(int j=0;j<4;j++){
                                        memDatos[posMem+j] = this.cacheDeDatos[j][posCache];
                                    }
                                           tenemos que contar 2 veces la latencia 
                                            while(i<mainThread.latencia){
                                                cambiarCiclo();
                                                i++;
                                            }
                                }*/
                                for(int j=0;j<4;j++){
                                    this.cacheDeDatos[j][posCache] =  memDatos[posMem+j];
                                }
                                this.cacheDeDatos[4][posCache]=numBloque;
                                this.cacheDeDatos[palabra][posCache] = dato;
                                this.estadoCacheDatos[posCache] = 'M';
                                mainThread.nucleos[this.otraCache].estadoCacheDatos[posCache] = 'I';
                                while(i<1){ //mainThread.latencia
                                    cambiarCiclo();
                                    i++;
                                }
                                liberarOtraCache();
                                liberarBusDatos();
                                liberarMiCache();
                                break;
                        }
                    }
                    else
                    {//cuando No está en NINGUNA caché
                        liberarOtraCache();
                        if(this.estadoCacheDatos[posCache]=='M'){ //si se encuentra ocupado con M el bloque que voy a sobreescribir  en caché
                            posMem = ((this.cacheDeDatos[4][posCache]*16)%640)/4; //donde se va a guardar en memoria el bloque a sobreescribir.
                            for(int j=0;j<4;j++){
                                memDatos[posMem+j] = this.cacheDeDatos[j][posCache]; //guardamos en memoria el bloque 
                            }
                            this.cacheDeDatos[4][posCache]=numBloque;
                            int i=0;      
                            while(i<1){ //mainThread.latencia
                                cambiarCiclo();
                                i++;
                            }
                        }
                        for(int j=0;j<4;j++){
                            this.cacheDeDatos[j][posCache] =  memDatos[posMem+j]; //ponemos en el bloque los datos de memoria que necesitamos
                        }
                        this.cacheDeDatos[4][posCache]=numBloque;
                        this.cacheDeDatos[palabra][posCache] = dato; 
                        this.estadoCacheDatos[posCache] = 'M';
                        liberarBusDatos();
                        liberarMiCache();
                    }
                }
                else
                {
                    instCompletada = false;
                }
            }
            
            this.quantumNucleo--;
        }
        
    }
    
    
    
    private void invalidar(int posCache){
        mainThread.invalidar[0] = this.otraCache;
        mainThread.invalidar[1] = posCache;
    }
    
    public boolean pedirMiCache(){
        boolean resp = false;
        if(this.comunicadores[this.numProcesador].semaforoCache.tryAcquire()){
            resp = true;
        }        
        return resp;
    }
    
    public void liberarMiCache(){
        this.comunicadores[this.numProcesador].semaforoCache.release();
    }
    
    public boolean pedirOtraCache(){
        boolean resp = false;
        if(this.comunicadores[this.otraCache].semaforoCache.tryAcquire()){
            resp = true;
        }        
        return resp;
    }
    
    public void liberarOtraCache(){
        this.comunicadores[this.otraCache].semaforoCache.release();
    }
    
    public boolean pedirBusDatos(){
        boolean resp = false;
        if(this.comunicadores[this.numProcesador].busCacheDatos.tryAcquire()){
            resp = true;
        }        
        return resp;
    }
    
    public void liberarBusDatos(){
        this.comunicadores[this.numProcesador].busCacheDatos.release();
    }
    
    public void ll(int inst2,int inst3){        
        this.comunicadores[this.numProcesador].vectreg[32]=inst3;  //se guarda la direccion del candado
        soyLoadLink=true;
        lw(0, inst2, inst3);
        
    }
    
    public void sc(int inst2,int inst3){        
        if(this.comunicadores[this.numProcesador].vectreg[32]==inst3){ //si mi dir de candado es la misma que está en RL
            this.comunicadores[this.numProcesador].vectreg[1]=1;
            mainThread.llActivo[0]=0;
            mainThread.llActivo[1]=0;
            mainThread.llActivo[2]=0;
            sw(0, inst2, inst3);  
            //si LL está activo hay que decirle al papá que invalide y ponga -| en ese RL
        }else{
            this.comunicadores[this.numProcesador].vectreg[1]=0; //no es atomica
        }
    }
}
