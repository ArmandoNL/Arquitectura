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

public class HiloControlador extends javax.swing.JFrame{
    
    public Comunicador[] comunicadores;//buzones para comunicación entre hilo principal y los hilos
    public Nucleo nucleo;//instancia de la clase nucleo
    public Nucleo[] nucleos;//vector para manejar varios nucleos
    public ArrayList<Integer> memTemp;//memoria temporal donde se almacenan las instrucciones
    public int ciclosReloj; //contador de ciclos de reloj
    public  CyclicBarrier barrier;//barrera cíclica para controlar el cambio de ciclo
    public int hilos; //para controlar el número de hilos con el que vamos a trabajar temporalmente
    private static int cantHilos;//para controlar el número de hilos con el que vamos a trabajar, valor definitivo
    public  Queue <Integer> vectPc;//cola para almacenar los PC y facilitar la asignación
    public  Queue <Integer> vectPcFinal;//cola para almacenar hasta dónde está almacenado cada archivo en la memorio
    private int[] vecPC = new int[20];//vector temporal para almacenar el valor inicial de los PC
    public int[] vecPcFinal = new int[20];//vector temporal para almacenar el valor final de los PC
    public int[] nombreArchivo = new int[15];//almacenar el nombre de los archivos con los que trabajamos.
    int contArchivos = 0;//número de archivos con que trabajamos
    private int numLineas;//número de líneas con que trabajamos
    
    //variables para el manejo de la interfaz
    JFileChooser fc;
    public int quantum;
    public int tiempoEspera;
    public int  tiempoBus;
    public int latencia;
    private int i;
	
    public HiloControlador() {
        //inicializamos las variables declaradas anteriormente
        initComponents();
        ciclosReloj = 0;
        memTemp = new ArrayList<Integer>();
        vectPc = new LinkedList<Integer>();
        vectPcFinal = new LinkedList<Integer>();
        numLineas=0;
        fc = new JFileChooser();
        i=0;
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
        textarea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        CicloReloj_TF = new javax.swing.JTextField();
        rdbModoLento = new javax.swing.JRadioButton();
        btnContinuar = new javax.swing.JButton();
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

        textarea.setColumns(20);
        textarea.setRows(5);
        jScrollPane1.setViewportView(textarea);

        jLabel1.setText("Ciclo actual del Reloj :");

        CicloReloj_TF.setEditable(false);

        rdbModoLento.setText("Activar Modo Lento");
        rdbModoLento.setToolTipText("");

        btnContinuar.setBackground(new java.awt.Color(0, 102, 153));
        btnContinuar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnContinuar.setText("Continuar");
        btnContinuar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContinuarActionPerformed(evt);
            }
        });

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
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(Ejecutar)
                                .addGap(33, 33, 33)
                                .addComponent(btnContinuar))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(lbLatencia)
                                .addGap(18, 18, 18)
                                .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(94, 94, 94)
                                .addComponent(lbTiempobus)
                                .addGap(18, 18, 18)
                                .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                                .addComponent(lbQuantum)
                                .addGap(18, 18, 18)
                                .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(22, 22, 22)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(232, 232, 232)
                .addComponent(jLabel1)
                .addGap(32, 32, 32)
                .addComponent(CicloReloj_TF, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rdbModoLento)
                .addGap(32, 32, 32))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(CicloReloj_TF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(rdbModoLento)))
                .addGap(21, 21, 21)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLatencia)
                    .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbTiempobus)
                    .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbQuantum)
                    .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Ejecutar)
                    .addComponent(btnContinuar))
                .addGap(29, 29, 29))
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
                ciclosReloj++;
                String ciclo=""+ciclosReloj;
                //ciclo=CicloReloj_TF.getText();
                CicloReloj_TF.setText(ciclo);
            }
            for(int i = 0; i < hilos; i++){  //cambiar
            	if(!comunicadores[i].ocupado){
                  System.out.println("VALOR COLA ates de if: " + vectPc.peek());
                    if(!vectPc.isEmpty()){
                        System.out.println("VALOR COLA: " + vectPc.peek());
                         
                        int pcActual= vectPc.poll();
                        System.out.println("Valor cola despues: " + vectPc.peek());
                        comunicadores[i].write(pcActual,quantum);
                        comunicadores[i].setPcFinal(vectPcFinal.poll());
                        comunicadores[i].ocupado = true;
                        comunicadores[i].seguir = true;
                        //comunicadores[i].semaforoComunicador.release();
                        
                    }
                    else{
                    	//si no hay archivos asignables, se le avisa al hilo que termino
                        comunicadores[i].write(-1, quantum);
                        comunicadores[i].ocupado = true;
                        comunicadores[i].terminado = true;
                        //comunicadores[i].semaforoComunicador.release();
                        
                    }
                }
            }
        }
    };
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
            tiempoBus = Integer.parseInt(txtTiempoBus.getText());
            latencia = 4*((2*tiempoBus)+tiempoEspera); // cuanddo se usa??
                       
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
          nombreArchivo[i]=Integer.parseInt(file.getName().substring(0,1));
          i++;
            System.out.println(file.getName().substring(0,1));
        Charset charset = Charset.forName("US-ASCII");
        try(BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)){
            
            //String lineaArchivo = "";
            int contLineas = 0;//variable para contar el # de lineas del archivo
            String lineaArchivo = reader.readLine();
            while(!"".equals(lineaArchivo) && lineaArchivo != null){//se lee cada una de las líneas del achivo
                contLineas++;//contamos las lineas del archivo para calcular el PC
                if("63 0 0 0".equals(lineaArchivo)){                    
                    int instfinal=contLineas;
                     nombreArchivo[i]=numLineas+instfinal*4;
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
                numLineas += contLineas*4;               
            }
            else
            {
                vecPC[contArchivos]= numLineas;               
                numLineas += contLineas*4;               
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
        // aqui hay que llamar a resume para cada hilo
    }//GEN-LAST:event_btnContinuarActionPerformed
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
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbLatencia;
    private javax.swing.JLabel lbQuantum;
    private javax.swing.JLabel lbTiempobus;
    public javax.swing.JRadioButton rdbModoLento;
    private javax.swing.JTextArea textarea;
    private javax.swing.JTextField txtLatencia;
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

        
