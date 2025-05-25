import java.rmi.RemoteException;
import java.awt.*;

public class ClientModel {
    private ClientPresenter p;
    private double xMinimum, xMaximum, yMinimum, yMaximum;
    private Color[][][] bild;
    private MasterInterface master;
    private int totalThread;
    private int indexstufenanzahl, indexstufenanzahlChunk;
    private int indexChunkY, rowsPerBlock;
    private int indexChunkX, columnPerBlock;
    private Thread[] threads;

    public ClientModel(ClientPresenter p, MasterInterface master) {
        this.p = p;
        this.master = master;
    }

    synchronized public void getChunk(){
        if(!p.stopVideo){
            if(p.yChunk > indexChunkY && totalThread > indexstufenanzahlChunk){
                if(p.xChunk > indexChunkX){
                    int yStartrt = indexChunkY * rowsPerBlock;
                    int y_end = (indexChunkY == p.yChunk - 1) ? p.ypix : yStartrt + rowsPerBlock;

                    int xStartrt = indexChunkX * columnPerBlock;
                    int x_end = (indexChunkX == p.xChunk - 1) ? p.xpix : xStartrt + columnPerBlock;

                    //System.out.println(xStartrt + "- " + x_end + " | " + yStartrt + " - " + y_end);

                    threads[indexstufenanzahlChunk] = new Thread(new MandelbrotWorker(yStartrt, y_end, xStartrt, x_end, indexstufenanzahl, p.maxIterations, xMinimum, xMaximum, yMinimum, yMaximum));
                    threads[indexstufenanzahlChunk].start();

                    indexChunkX++;
                    indexstufenanzahlChunk++;
                }else{
                    indexChunkX = 0;
                    indexChunkY++;
                    getChunk();
                }
            }else if(p.stufenanzahl > indexstufenanzahl){
                p.maxIterations = (int)(p.maxIterations + p.zoomfaktor * p.add_iter);
                double xdim = xMaximum - xMinimum;
                double ydim = yMaximum - yMinimum;
                xMinimum = p.cr - xdim / 2 / p.zoomfaktor;
                xMaximum = p.cr + xdim / 2 / p.zoomfaktor;
                yMinimum = p.ci - ydim / 2 / p.zoomfaktor;
                yMaximum = p.ci + ydim / 2 / p.zoomfaktor;
                if(!p.hide_process){
                    p.v.update(bild[indexstufenanzahl]);
                }
                //p.updateInfo("stufenanzahl: " + (indexstufenanzahl + 1) + " | Max-Iterations: " + p.maxIterations);
                indexChunkY = 0;
                indexstufenanzahl++;
                getChunk();
            }else{
                System.out.println("Ende");
            }
        }else{
            System.out.println("Forced Stop!");
            for (; indexstufenanzahlChunk < totalThread; indexstufenanzahlChunk++) {
                threads[indexstufenanzahlChunk] = new Thread();
            }
        }
        p.currentTime = System.currentTimeMillis();
        p.v.update_zeit(p.currentTime - p.startTime);
        p.v.updateInfo("Chunks: " + indexstufenanzahlChunk + "/" + totalThread + " | stufenanzahl: " + indexstufenanzahl + " | Max-Iterations: " + p.maxIterations + " | Threads: " + Thread.activeCount());
    }

    /** Erzeuge ein komplettes Bild mit Threads */
    Color[][][] mandelbrotImage(double xMinimum, double xMaximum, double yMinimum, double yMaximum) {
        this.xMinimum = xMinimum;
        this.xMaximum = xMaximum;
        this.yMinimum = yMinimum;
        this.yMaximum = yMaximum;

        bild = new Color[p.stufenanzahl][p.xpix][p.ypix];
        indexstufenanzahl = 0;
        indexstufenanzahlChunk = 0;

        indexChunkY = 0;
        rowsPerBlock = p.ypix / p.yChunk;

        indexChunkX = 0;
        columnPerBlock = p.xpix / p.xChunk;

        totalThread = p.xChunk * p.yChunk * p.stufenanzahl;

        threads = new Thread[totalThread];

        p.startTime = System.currentTimeMillis();

        for (int i = 0; i < p.client_threads; i++) {
            getChunk();
        }

        for (int i = 0; i < totalThread; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return bild;
    }

    class MandelbrotWorker implements Runnable {
        int worker_yStartrt, worker_yStopp, worker_xStartrt, worker_xStopp;
        int worker_stufenanzahl, worker_maxIterations;
        double worker_xMinimum, worker_xMaximum, worker_yMinimum, worker_yMaximum;

        public MandelbrotWorker(int worker_yStartrt, int worker_yStopp, int worker_xStartrt, int worker_xStopp, int worker_stufenanzahl, int worker_maxIterations, double worker_xMinimum, double worker_xMaximum, double worker_yMinimum, double worker_yMaximum) {
            this.worker_yStartrt = worker_yStartrt;
            this.worker_yStopp = worker_yStopp;
            this.worker_xStartrt = worker_xStartrt;
            this.worker_xStopp = worker_xStopp;
            this.worker_stufenanzahl = worker_stufenanzahl;
            this.worker_maxIterations = worker_maxIterations;
            this.worker_xMinimum = worker_xMinimum;
            this.worker_xMaximum = worker_xMaximum;
            this.worker_yMinimum = worker_yMinimum;
            this.worker_yMaximum = worker_yMaximum;
        }

        @Override
        public void run() {
            try {
                int resultY_index = 0;
                int resultX_index = 0;
                int[][] result = master.calculateMandelbrotImage(p.workersThreads, worker_maxIterations, p.maxBetrag, worker_yStartrt, worker_yStopp,  worker_xStartrt, worker_xStopp, p.xpix, p.ypix, worker_xMinimum, worker_xMaximum, worker_yMinimum, worker_yMaximum);
                for (int y = worker_yStartrt; y < worker_yStopp; y++) {
                    for (int x = worker_xStartrt; x < worker_xStopp; x++) {
                        int iter = result[resultX_index][resultY_index];
                        
                        /* if(iter == maxIterations){
                            bild[this_stufenanzahl][x][y] = Color.BLACK;
                        }else{
                            float c = (float) iter / maxIterations * p.farbe_number;
                            bild[this_stufenanzahl][x][y] = Color.getHSBColor(c, 1f, 1f);
                        } */

                        if (iter == worker_maxIterations) {
                            if(p.show_chunk_line && (y == worker_yStopp - 1 || x == worker_xStopp - 1)){
                                bild[worker_stufenanzahl][x][y] = Color.getHSBColor(1f, 1f, 1f);
                            }else{
                                bild[worker_stufenanzahl][x][y] = Color.BLACK;
                            }
                        } else {
                            if(p.show_chunk_line && (y == worker_yStopp - 1 || x == worker_xStopp - 1)){
                                bild[worker_stufenanzahl][x][y] = Color.BLACK;
                            }else{
                                float c = (float) iter / worker_maxIterations * p.farbe_number;
                                bild[worker_stufenanzahl][x][y] = Color.getHSBColor(c, 1f, 1f);
                            }
                        }
                        resultX_index++;
                    }
                    resultX_index = 0;
                    resultY_index++;
                }
                getChunk();
            } catch (RemoteException e) {
                p.stopVideo = true;
                p.v.updateInfo("Thread Error!");
            }
        }
    }
}
