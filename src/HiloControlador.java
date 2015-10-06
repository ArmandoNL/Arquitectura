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
    public Nucleo[] nucleos;
    public ArrayList<Integer> memTemp;
    private int ciclosReloj; 
    private int PC;
    public  CyclicBarrier barrier;
    private Semaphore semaforo;
    private int idHilos;
    private int hilos; 
    private int semaforoComunicador;
    private static final int cantHilos = 2;
    public static Queue <Integer> vectPc;
    JFileChooser fc;
    public int quantum;
    public int tiempoEspera;
    public int  tiempoBus;
    public int latencia;
    private int numLineas;
    private Queue<int[]> contextos;
    int[] vecPC = new int[10];
        int contArchivos = 0;
	
    public HiloControlador() {
        initComponents();
        //hilos = 1;
        //cantHilos = hilos;
        ciclosReloj = 0;
        memTemp = new ArrayList<Integer>();
        vectPc = new LinkedList<Integer>();
        PC = 0;
        numLineas=0;
        //barrier = new CyclicBarrier(cantHilos, barrierFuncion);
        fc = new JFileChooser();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        Ejecutar = new javax.swing.JButton();
        lbLatencia = new javax.swing.JLabel();
        lbTiempobus = new javax.swing.JLabel();
        lbQuantum = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
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

        Ejecutar.setText("Ejecutar");
        Ejecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EjecutarActionPerformed(evt);
            }
        });

        lbLatencia.setText("Latencia");

        lbTiempobus.setText("Tiempo Bus");

        lbQuantum.setText("Quantum");

        textarea.setColumns(20);
        textarea.setRows(5);
        jScrollPane1.setViewportView(textarea);

        jMenuBar1.setBackground(new java.awt.Color(204, 204, 204));

        File.setText("File");
        File.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileActionPerformed(evt);
            }
        });

        Open.setText("Open");
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 674, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 25, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbTiempobus)
                                    .addComponent(lbLatencia))
                                .addGap(60, 60, 60)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbQuantum)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(Ejecutar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLatencia)
                    .addComponent(txtLatencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbTiempobus)
                    .addComponent(txtTiempoBus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbQuantum)
                    .addComponent(txtQuantum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(61, 61, 61)
                .addComponent(Ejecutar)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    Runnable barrierFuncion = new Runnable(){
        public void run(){   
           ciclosReloj++;   //chequear quantum    
            for(int i = 0; i < hilos; i++){  //cambiar
            	if(!comunicadores[i].ocupado){
                    int pcActual= vectPc.poll();
                    if(pcActual != -1){
                        comunicadores[i].write(pcActual,quantum);
                        comunicadores[i].semaforoComunicador.release();
                    }
                    else{
                    	//si no hay archivos asignables, se le avisa al hilo que termino
                        comunicadores[i].write(-1, quantum);
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
            for(int i = 0; i < hilos; i++){
    		comunicadores[i] = new Comunicador();
            }
            vectPc.add(0);
            vectPc.add(64); //llenar vector
            
            for(int i = 0; i< hilos; i++){
             if(vectPc.size() != 0){
                comunicadores[i].write(vectPc.poll(), quantum);
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
            contArchivos++;
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
                vectPc.add(0);
                vecPC[contArchivos]= 0;
                numLineas += contLineas*4;
            }
            else
            {
                vectPc.add(numLineas);
                vecPC[contArchivos]= numLineas;
                numLineas += contLineas*4;
            }
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
        for(int i =0; i<vectPc.size(); i++){
            int value = vectPc.poll();
	    System.out.println("PC: " + value);
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
    private javax.swing.JSeparator jSeparator1;
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

        
