import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HiloControlador extends javax.swing.JFrame{
    
    public Comunicador[] comunicadores;//buzones para comunicación entre hilo principal y los hilos
    public Nucleo nucleo;//instancia de la clase nucleo
    public Nucleo[] nucleos;//vector para manejar varios nucleos
    public ArrayList<Integer> memTemp;//memoria temporal donde se almacenan las instrucciones
    public int[] memDatos;
    public int ciclosReloj; //contador de ciclos de reloj
    public  CyclicBarrier barrier;//barrera cíclica para controlar el cambio de ciclo
    public int hilos; //para controlar el número de hilos con el que vamos a trabajar temporalmente
    private static int cantHilos;//para controlar el número de hilos con el que vamos a trabajar, valor definitivo
    public  Queue <Integer> vectPc;//cola para almacenar los PC y facilitar la asignación
    public  Queue <Integer> vectPcFinal;//cola para almacenar hasta dónde está almacenado cada archivo en la memorio
    private int[] vecPC = new int[20];//vector temporal para almacenar el valor inicial de los PC
    public int[] vecPcFinal = new int[20];//vector temporal para almacenar el valor final de los PC
    public int[] nombreArchivo = new int[25];//almacenar el nombre de los archivos con los que trabajamos.
    int contArchivos = 0;//número de archivos con que trabajamos
    private int numLineas;//número de líneas con que trabajamos
    public boolean continuar = false;
    public int[] invalidar;
    public int[]  llActivo;
    //private final static Lock busCacheInst = new Lock();
    private final static Semaphore busCacheInst = new Semaphore(1);
    private final static Semaphore busCacheDatos = new Semaphore(1);
    public ConcurrentLinkedQueue<int[]> contextos;
    
    //variables para el manejo de la interfaz
    JFileChooser fc;
    public int quantum;
    public int tiempoEspera;
    public int  tiempoBus;
    public int latencia;
    private int i=0;
    private int j=0;
	
    public HiloControlador() {
        //inicializamos las variables declaradas anteriormente
        initComponents();
        ciclosReloj = 0;
        memTemp = new ArrayList<Integer>();
        memDatos = new int[353];
        vectPc = new LinkedList<Integer>();
        vectPcFinal = new LinkedList<Integer>();
        numLineas=0;
        fc = new JFileChooser();
        //i=0;
        invalidar = new int[2];
        invalidar[0] = -1;
        llActivo= new int[3];
        llActivo[0]=0;
        contextos = new ConcurrentLinkedQueue<int[]>();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        Ejecutar = new javax.swing.JButton();
        lbLatencia = new javax.swing.JLabel();
        lbTiempobus = new javax.swing.JLabel();
        lbQuantum = new javax.swing.JLabel();
        txtLatencia = new javax.swing.JTextField();
        txtTiempoBus = new javax.swing.JTextField();
        txtQuantum = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        ta_DatosLento0 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        CicloReloj_TF = new javax.swing.JTextField();
        rdbModoLento = new javax.swing.JRadioButton();
        btnContinuar = new javax.swing.JButton();
        btnParar = new javax.swing.JButton();
        txtProc0 = new javax.swing.JTextField();
        txtProc1 = new javax.swing.JTextField();
        lbProc1 = new javax.swing.JLabel();
        lbProc2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textarea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        ta_Datos = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        ta_DatosLento1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        File = new javax.swing.JMenu();
        Open = new javax.swing.JMenuItem();
        Exit = new javax.swing.JMenuItem();

        fileChooser.setDialogTitle("Open Dialog Menu");
        fileChooser.setFileFilter(new CustomFilter());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(110, 153, 167));
        setForeground(new java.awt.Color(49, 95, 95));

        Ejecutar.setBackground(new java.awt.Color(0, 102, 153));
        Ejecutar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Ejecutar.setForeground(new java.awt.Color(255, 255, 255));
        Ejecutar.setText("Ejecutar");
        Ejecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EjecutarActionPerformed(evt);
            }
        });

        lbLatencia.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbLatencia.setText("Latencia");

        lbTiempobus.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbTiempobus.setText("Tiempo Bus");

        lbQuantum.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbQuantum.setText("Quantum");

        ta_DatosLento0.setColumns(20);
        ta_DatosLento0.setRows(5);
        jScrollPane1.setViewportView(ta_DatosLento0);

        jLabel1.setText("Ciclo actual del Reloj :");

        CicloReloj_TF.setEditable(false);

        rdbModoLento.setText("Activar Modo Lento");
        rdbModoLento.setToolTipText("");

        btnContinuar.setBackground(new java.awt.Color(0, 102, 153));
        btnContinuar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnContinuar.setForeground(new java.awt.Color(255, 255, 255));
        btnContinuar.setText("Continuar");
        btnContinuar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContinuarActionPerformed(evt);
            }
        });

        btnParar.setBackground(new java.awt.Color(102, 0, 0));
        btnParar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnParar.setForeground(new java.awt.Color(255, 255, 255));
        btnParar.setText("Terminar");
        btnParar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPararActionPerformed(evt);
            }
        });

        txtProc0.setEditable(false);

        txtProc1.setEditable(false);
        txtProc1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProc1ActionPerformed(evt);
            }
        });

        lbProc1.setText("Archivo en Proc 0");

        lbProc2.setText("Archivo en Proc 1");

        textarea.setColumns(20);
        textarea.setRows(5);
        jScrollPane2.setViewportView(textarea);

        ta_Datos.setColumns(20);
        ta_Datos.setRows(5);
        jScrollPane3.setViewportView(ta_Datos);

        ta_DatosLento1.setColumns(20);
        ta_DatosLento1.setRows(5);
        jScrollPane4.setViewportView(ta_DatosLento1);

        jMenuBar1.setBackground(new java.awt.Color(204, 204, 204));

        File.setBackground(new java.awt.Color(204, 204, 255));
        File.setText("Seleccionar Archivos");
        File.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileActionPerformed(evt);
            }
        });

        Open.setText("Abrir");
        Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenActionPerformed(evt);
            }
        });
        File.add(Open);

        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });
        File.add(Exit);

        jMenuBar1.add(File);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnParar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnContinuar)
                        .addGap(27, 27, 27)
                        .addComponent(Ejecutar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(lbLatencia)
                        .addGap(27, 27, 27)
                        .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94)
                        .addComponent(lbTiempobus)
                        .addGap(18, 18, 18)
                        .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbQuantum)
                        .addGap(38, 38, 38)
                        .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(176, 176, 176))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbProc2)
                    .addComponent(lbProc1))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtProc1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtProc0, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(CicloReloj_TF, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rdbModoLento)
                        .addGap(31, 31, 31))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbProc1)
                    .addComponent(txtProc0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(CicloReloj_TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbModoLento)
                    .addComponent(lbProc2)
                    .addComponent(txtProc1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 27, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLatencia)
                    .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbTiempobus)
                    .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbQuantum)
                    .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Ejecutar)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnParar)
                        .addComponent(btnContinuar)))
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /*
      Efecto: verifica el estado del hilo y asigna un pc de la cola 
      Requiere: que la colo est[r inicializada
      Modifica: el PC del hilo
    */
    Runnable barrierFuncion = new Runnable(){
        public void run(){   
            if(comunicadores[0].cambiarCiclo && comunicadores[1].cambiarCiclo){
                if(invalidar[0]!=-1){
                    if(llActivo[0]==1 && invalidar[0]==llActivo[1] && invalidar[1]==llActivo[2]){ //si el LL está activo y está en la misma caché y el mismo bloque donde se está mandando a invalidar.
                        //guardo en RL un -1 en Rl si el LL está activo cuando se envia a invalidar.
                        comunicadores[llActivo[1]].vectreg[32]=-1;
                        llActivo[0]=-1;  //se desactiva el LL
                        llActivo[1]=-1;
                        llActivo[2]=-1;
                        
                    }
                    nucleos[invalidar[0]].estadoCacheDatos[invalidar[1]]='I'; 
                    invalidar[0]=-1;
                }
                ciclosReloj++;
                String ciclo=""+(ciclosReloj-1);
                CicloReloj_TF.setText(ciclo);
                if(rdbModoLento.isSelected()){
                  try{
                      do{
                          Thread.sleep(100);
                        }while(!continuar);
                  }catch(InterruptedException e){
                  }
                  continuar = false;    
                }
                
            }
            
            for(int i = 0; i < hilos; i++){  //cambiar
            	if(!comunicadores[i].ocupado){
                  System.out.println("VALOR COLA : " + vectPc.peek());
                    if(!vectPc.isEmpty()){
                        System.out.println("VALOR COLA: " + vectPc.peek());
                        int pcActual= vectPc.poll();
                        System.out.println("Valor cola despues: " + vectPc.peek());
                        comunicadores[i].write(pcActual,quantum);
                        comunicadores[i].setPcFinal(vectPcFinal.poll());
                        comunicadores[i].ocupado = true;
                        comunicadores[i].seguir = true;
                        
                        
                    }
                    else{
                    	//si no hay archivos asignables, se le avisa al hilo que termino
                        comunicadores[i].write(-1, quantum);
                        comunicadores[i].ocupado = true;
                        comunicadores[i].terminado = true;
                        
                        
                    }
                }
            }
        }
    };
    
     /*  Efecto: se encarga de obtener el bus de datos devolviendo true, si no devuelve falso.
    Requiere: que un procesador pida el bus.
    Modifica: el valor del bus.
    */
    public static boolean pedirBusDatos(){
        return busCacheDatos.tryAcquire();
    }
    
    public static void liberarBusDatos(){
        busCacheDatos.release();
    }
    
    
    /*  Efecto: se encarga de obtener el bus devolviendo true, si no devuelve falso.
    Requiere: que un procesador pida el bus.
    Modifica: el valor del bus.
    */
    public static boolean pedirBusInst(){
        return busCacheInst.tryAcquire();
    }
    
    public static void liberarBusInst(){
        busCacheInst.release();
    }

       //retorna un vector con un los valores del contexto y los borra de la cola de contextos.
       public int[] pedirContexto()
       {
           return contextos.poll();
       }
       
       //guarda en la cola de contextos, los registros.
       public void guardarContexto(int[] vec)
       {
           contextos.add(vec);
       }
    
    
        /*
          Efecto: asigna la primera vez los PC, inicializa los nucleos y crea los hilos a ejecutar.
          Requiere: que se presione el botón Ejecutar en la interfaz
          Modifica: variables como: cola de PC, núcleos y buzones 
        */
	private void EjecutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EjecutarActionPerformed
            if(hilos==1){
                cantHilos = hilos;
             }else {
                hilos = 2;
                cantHilos = 2;
            }
            barrier = new CyclicBarrier(cantHilos, barrierFuncion);
            quantum = Integer.parseInt(txtQuantum.getText());
            tiempoEspera = Integer.parseInt(txtLatencia.getText());
            tiempoBus = 0;//Integer.parseInt(txtTiempoBus.getText());
            latencia = 4*((2*tiempoBus)+tiempoEspera); // cuanddo se usa??
            
                 
            for(int i = 0; i < 353; i++){
                memDatos[i] = 1; 
            }   
            comunicadores = new Comunicador[2];
            for(int i = 0; i < 2; i++){
    		comunicadores[i] = new Comunicador();
            }
            
            for(int i = 0; i<contArchivos;++i)//se agregan los PC a la cola
            {
                vectPc.add(vecPC[i]);
                vectPcFinal.add(vecPcFinal[i]);
            }
            
            for(int i = 0; i< hilos; i++){
             if(vectPc.size() != 0){
                comunicadores[i].write(vectPc.poll(), quantum);
                comunicadores[i].setPcFinal(vectPcFinal.poll());
             }
                  //comunicadores[1].write(vectPc.poll(), quantum);
            else
            {
                System.out.println("Se acabo programa");
            }
            }
            nucleos = new Nucleo[2];
            for(int i = 0; i < hilos; i++){
    		nucleos[i] = new Nucleo(this, i);
            }           
        
          for(int i = 0; i < hilos; i++){
    		(new Thread(nucleos[i])).start();
            }   
           // metodoPrincipal();
    }//GEN-LAST:event_EjecutarActionPerformed

    private void FileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FileActionPerformed

    }//GEN-LAST:event_FileActionPerformed

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        System.exit(0); 
    }//GEN-LAST:event_ExitActionPerformed
   
    /*
      Efecto: lee los archivos y agrega los PC al vector de PCs temporal 
      Requiere: la entrada de un archivo
      Modifica: el vector de PC temporal y la memoria temporal de instrucciones: memTemp
    */
    private void OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenActionPerformed
        
//inicia manejo de archivos     
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();  
            if(!"-".equals(file.getName().substring(4,5))){
                nombreArchivo[i]=Integer.parseInt(file.getName().substring(4,5));
            }else{
                nombreArchivo[i]=-1;
            }
            i++;
            System.out.println("Este es en nombre   "+file.getName().substring(4,5));
        Charset charset = Charset.forName("US-ASCII");
        try(BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)){
            
            //String lineaArchivo = "";
            int contLineas = 0;//variable para contar el # de lineas del archivo
            String lineaArchivo = reader.readLine();
            while(!"".equals(lineaArchivo) && lineaArchivo != null){//se lee cada una de las líneas del achivo
                contLineas++;//contamos las lineas del archivo para calcular el PC
                if("63 0 0 0".equals(lineaArchivo)|| "63 0 0 0 ".equals(lineaArchivo)){                    
                    int instfinal=contLineas;
                     nombreArchivo[i]=numLineas+instfinal*4;
                      //System.out.println("esta es la 63   "+nombreArchivo[i]);
                    i++;
                }
                String[] instrucciones = lineaArchivo.split(" ");
                for(int i=0; i<instrucciones.length;++i){
                    memTemp.add(Integer.parseInt(instrucciones[i]));
                }
                lineaArchivo = reader.readLine();
            }
            //finaliza manejo de archivos
            
            if(numLineas == 0)//se almacenan los PC
            {
                vecPC[contArchivos]= 0;
                nombreArchivo[i]= 0;
               // System.out.println(nombreArchivo[i]);
                numLineas += contLineas*4; 
                i++;
            }
            else
            {
                vecPC[contArchivos]= numLineas; 
                nombreArchivo[i]= numLineas;
               // System.out.println(nombreArchivo[i]);
                numLineas += contLineas*4; 
                i++;
            }
            vecPcFinal[contArchivos] = numLineas;
            contArchivos++;
        }
        catch(IOException exc){
            System.err.println("IOException error");
        }
        
        }else{
            System.out.println("File access cancelled by user.");
        }       
        hilos+=1;                
    }//GEN-LAST:event_OpenActionPerformed

    private void btnContinuarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContinuarActionPerformed
       continuar = true;
    }//GEN-LAST:event_btnContinuarActionPerformed

    private void btnPararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPararActionPerformed
         System.exit(0); 
    }//GEN-LAST:event_btnPararActionPerformed

    private void txtProc1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProc1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtProc1ActionPerformed
    
    /*
      Efecto: Imprime en la interfaz los registros,el quantum y otros datos
      Requiere: Un string con los datos a mostrar 
      Modifica: El texarea de la Interfaz 
    */
   public void imprimirPantalla(String texto){
       String t="";
       t=textarea.getText();
       t+=texto;
       textarea.setText(t);
    }
   
   /*
      Efecto: Imprime en la interfaz las caches de Datos
      Requiere: Un string con los datos a mostrar 
      Modifica: El texarea de la Interfaz 
    */
   public void imprimirPantallaDatos(String texto){
       String t="";
       t=ta_Datos.getText();
       t+=texto;
       ta_Datos.setText(t);
    }
   
   /*
      Efecto: Imprime en la interfaz el archivo que se esta corriendo en el nucleo 0
      Requiere: Un string con los datos a mostrar 
      Modifica: El textField de la Interfaz  
    */
    public void imprimirEstado0(String texto){
       String t=""+texto;
       txtProc0.setText(t);
    }
    
    /*
      Efecto: Imprime en la interfaz el archivo que se esta corriendo en el nucleo 1
      Requiere: Un string con los datos a mostrar 
      Modifica: El textField de la Interfaz 
    */
     public void imprimirEstado1(String texto){
       String t=""+texto;
       txtProc1.setText(t);
    }
     
     /*
      Efecto: Imprime en la interfaz la cache de datos 0 
      Requiere: Nada 
      Modifica: El texarea de la Interfaz  
    */
     public void imprimirCacheDatos0(){
         String datos="";
         datos=" Valor de la Cache de Datos del nucleo: "+ 0 + "\n\n";
          for(int i =0; i<4; i++){
              switch(i){
                       case 0:
                             datos+=" Pal #1: ";           
                          break;
                        case 1:
                             datos+=" Pal #2: ";
                          break;
                        case 2:
                             datos+=" Pal #3: ";
                          break;
                        case 3:
                            datos+="  Pal #4: ";
                          break;                        
                        default:
                          break;
                   
                   }
               for(int j =0; j<8; j++){
                   
                   datos+=nucleos[0].cacheDeDatos[i][j]+"     ";
               }
               datos+="\n";
           }
          
          for(int i =4; i<5; i++){
              
               datos+=" NumBloc:    ";
                          
               for(int j =0; j<8; j++){
                   
                   datos+=nucleos[0].cacheDeDatos[i][j]+"    ";
               }
               datos+="\n";
           }
            datos+="\n";
         
         String t=""+datos;
         ta_DatosLento0.setText(t);
     }     
     
     /*
      Efecto: Imprime en la interfaz la cache de datos 1
      Requiere: Nada 
      Modifica: El texarea de la Interfaz 
    */
     public void imprimirCacheDatos1(){
         String datos="";
         datos=" Valor de la Cache de Datos del nucleo: "+ 1 + "\n\n";
          for(int i =0; i<4; i++){
              switch(i){
                       case 0:
                             datos+=" Pal #1: ";           
                          break;
                        case 1:
                             datos+=" Pal #2: ";
                          break;
                        case 2:
                             datos+=" Pal #3: ";
                          break;
                        case 3:
                            datos+="  Pal #4: ";
                          break;                        
                        default:
                          break;                   
                   }
               for(int j =0; j<8; j++){
                   
                   datos+=nucleos[1].cacheDeDatos[i][j]+"     ";
               }
               datos+="\n";
           }
          
          for(int i =4; i<5; i++){
              
               datos+=" NumBloc:    ";
                          
               for(int j =0; j<8; j++){
                   
                   datos+=nucleos[1].cacheDeDatos[i][j]+"    ";
               }
               datos+="\n";
           }
            datos+="\n";
         
         String t=""+datos;
         ta_DatosLento1.setText(t);
     }     
  
    //se encarga de la ejecución del programa
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HiloControlador().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CicloReloj_TF;
    private javax.swing.JButton Ejecutar;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenu File;
    private javax.swing.JMenuItem Open;
    private javax.swing.JButton btnContinuar;
    private javax.swing.JButton btnParar;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lbLatencia;
    private javax.swing.JLabel lbProc1;
    private javax.swing.JLabel lbProc2;
    private javax.swing.JLabel lbQuantum;
    private javax.swing.JLabel lbTiempobus;
    public javax.swing.JRadioButton rdbModoLento;
    private javax.swing.JTextArea ta_Datos;
    private javax.swing.JTextArea ta_DatosLento0;
    private javax.swing.JTextArea ta_DatosLento1;
    private javax.swing.JTextArea textarea;
    private javax.swing.JTextField txtLatencia;
    private javax.swing.JTextField txtProc0;
    private javax.swing.JTextField txtProc1;
    private javax.swing.JTextField txtQuantum;
    private javax.swing.JTextField txtTiempoBus;
    // End of variables declaration//GEN-END:variables
}

class CustomFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File file) {
            // Allow only directories, or files with ".txt" extension
            return file.isDirectory() || file.getAbsolutePath().endsWith(".txt");
        }
        @Override
        public String getDescription() {
            // This description will be displayed in the dialog,
            // hard-coded = ugly, should be done via I18N
            return "Text documents (*.txt)";
        }
    } 

        
