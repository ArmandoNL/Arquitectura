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
    
    public Comunicador[] comunicadores;
    public Nucleo nucleo;
    public Nucleo[] nucleos;
    public ArrayList<Integer> memTemp;
    private int ciclosReloj; 
    private int PC;
    public  CyclicBarrier barrier;
    private Semaphore semaforo;
    private int idHilos;
    public int hilos; 
    private int semaforoComunicador;
    private static int cantHilos = 2;
    public  Queue <Integer> vectPc;
    public  Queue <Integer> vectPcFinal;
    JFileChooser fc;
    public int quantum;
    public int tiempoEspera;
    public int  tiempoBus;
    public int latencia;
    private int numLineas;
    private int[] vecPC = new int[20];
    public int[] vecPcFinal = new int[20];
    int contArchivos = 0;
    public int contterminados;
	
    public HiloControlador() {
        initComponents();
        //hilos = 1;
        //cantHilos = hilos;
        ciclosReloj = 0;
        memTemp = new ArrayList<Integer>();
        vectPc = new LinkedList<Integer>();
        vectPcFinal = new LinkedList<Integer>();
        PC = 0;
        numLineas=0;
        //barrier = new CyclicBarrier(cantHilos, barrierFuncion);
        fc = new JFileChooser();
        contterminados =1;
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
        Ejecutar.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
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

        textarea.setColumns(20);
        textarea.setRows(5);
        jScrollPane1.setViewportView(textarea);

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Ejecutar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addGap(115, 115, 115)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLatencia)
                    .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbTiempobus)
                    .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbQuantum)
                    .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(117, 117, 117)
                .addComponent(Ejecutar)
                .addGap(31, 31, 31))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    Runnable barrierFuncion = new Runnable(){
        public void run(){   
          // ciclosReloj++;   //chequear quantum    
            for(int i = 0; i < hilos; i++){  //cambiar
            	if(!comunicadores[i].ocupado){
                  
                   // int pcActual= vectPc.poll();
                    if(vectPc.size()!=0){
                        int pcActual= vectPc.poll();
                        comunicadores[i].write(pcActual,quantum);
                        comunicadores[i].setPcFinal(vectPcFinal.poll());
                        comunicadores[i].ocupado = true;
                        comunicadores[i].seguir = true;
                        comunicadores[i].semaforoComunicador.release();
                        
                    }
                    else{
                    	//si no hay archivos asignables, se le avisa al hilo que termino
                        comunicadores[i].write(-1, quantum);
                        comunicadores[i].ocupado = true;
                        comunicadores[i].terminado = true;
                        comunicadores[i].semaforoComunicador.release();
                        
                    }
                }
            }
        }
    };
     
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

    private void OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenActionPerformed
     
        
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();             
         
        Charset charset = Charset.forName("US-ASCII");
        try(BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)){
            
            String lineaArchivo = " ";
            int contLineas = 0;//variable para contar el # de lineas del archivo
            while(!"".equals(lineaArchivo = reader.readLine())){
                contLineas++;//contamos las lineas del archivo para calcular el PC
                String[] instrucciones = lineaArchivo.split(" ");
                for(int i=0; i<instrucciones.length;++i){
                    memTemp.add(Integer.parseInt(instrucciones[i]));
                }
            }
            if(numLineas == 0)
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
        imprimirMem(); 
        
    }//GEN-LAST:event_OpenActionPerformed

   
    
    private void imprimirMem(){
        for(int i =0; i<memTemp.size(); i++){
            int value = memTemp.get(i);
	    System.out.println("Element: " + value);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HiloControlador().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Ejecutar;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenu File;
    private javax.swing.JMenuItem Open;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbLatencia;
    private javax.swing.JLabel lbQuantum;
    private javax.swing.JLabel lbTiempobus;
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

        
