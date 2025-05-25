import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Worker extends UnicastRemoteObject implements WorkerInterface {

    Worker() throws RemoteException {}

    @Override
    synchronized public int[][] calculateMandelbrotImage_worker(int workersThreads, int maxIterations, double maxBetrag, int yStart, int yStop, int xStart, int xStop, int xpix, int ypix, double xMinimum, double xMaximum, double yMinimum, double yMaximum) throws RemoteException {
        int current_y_length = yStop - yStart;
        int current_x_length = xStop - xStart;

        int[][] colors = new int[current_x_length][current_y_length];

        Thread[] threads = new Thread[workersThreads];
        int rowsPerThread = (current_y_length) / workersThreads;

        double dx = xMaximum - xMinimum;
        double dy = yMaximum - yMinimum;

        for (int i = 0; i < workersThreads; i++) {
            int current_yStartrt = i * rowsPerThread;
            int yStartrt = current_yStartrt + yStart;
            int y_end = (i == workersThreads - 1) ? yStop : yStartrt + rowsPerThread;

            threads[i] = new Thread(() -> {
                double c_re, c_im;
                int current_yStartrt_index = current_yStartrt;

                for (int y = yStartrt; y < y_end; y++) {
                    c_im = yMinimum + dy * y / ypix;
                    for (int x = 0; x < current_x_length; x++) {
                        c_re = xMinimum + dx * (xStart + x) / xpix;
                        int iter = calculation(maxIterations, maxBetrag, c_re, c_im);
                        colors[x][current_yStartrt_index] = iter;
                    }
                    current_yStartrt_index++;
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < workersThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return colors;
    }

    private int calculation(int maxIterations, double maxBetrag, double cr, double ci) {
        int iter;
        double zr = 0, zi = 0, zr2 = 0, zi2 = 0, zri = 0, betrag = 0;
        for (iter = 0; iter < maxIterations && betrag <= maxBetrag; iter++) {
            zr = zr2 - zi2 + cr;
            zi = zri + zri + ci;

            zr2 = zr * zr;
            zi2 = zi * zi;
            zri = zr * zi;
            betrag = zr2 + zi2;
        }
        return iter;
    }

    public static void main(String[] args) {
        if (args.length == 2){
            try {
                String masterIP = args[0];
                int masterPort = Integer.parseInt(args[1]);

                MasterInterface master = (MasterInterface) java.rmi.registry.LocateRegistry.getRegistry(masterIP, masterPort).lookup("MasterServer");
                Worker worker = new Worker();

                master.workerLogin(worker);

                System.out.println("Worker hat Verbindung zum Master-Port: " + masterPort + " hergestellt\n\n");
            } catch (Exception e) {
                System.err.println("Worker exception:");
                e.printStackTrace();
            }
        }else{
            System.out.println("Erforderliche Parameter: <Master IP> <Master Port>");
        }
    }
}