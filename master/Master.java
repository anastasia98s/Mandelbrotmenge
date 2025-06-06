import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Master extends UnicastRemoteObject implements MasterInterface {
    private List<WorkerManager> worker_manager_list = new ArrayList<>();

    private class WorkerManager {
        public WorkerInterface worker;
        public int aufgabe = 0;
        public WorkerManager(WorkerInterface worker) {
            this.worker = worker;
        }
        public void worker_arbeit_start() {
            aufgabe++;
        }
        public void worker_arbeit_end() {
            aufgabe--;
        }
    }

    private WorkerManager searchWorker() throws RemoteException{
        WorkerManager selected_worker_manager = null;
        for (WorkerManager this_worker_manager : worker_manager_list) {
            //System.out.println(this_worker_manager.worker + ", " + this_worker_manager.aufgabe);
            if (this_worker_manager.aufgabe == 0) {
                return this_worker_manager;
            }else if (selected_worker_manager == null) {
                selected_worker_manager = this_worker_manager;
            }else if (this_worker_manager.aufgabe < selected_worker_manager.aufgabe) {
                selected_worker_manager = this_worker_manager;
            }
        }
        return selected_worker_manager;
    }

    Master() throws RemoteException {}

    @Override
    public void workerLogin(WorkerInterface worker){
        WorkerManager worker_manager = new WorkerManager(worker);
        worker_manager_list.add(worker_manager);
        System.out.println("New Worker!");
    }

    @Override
    public int[][] calculateMandelbrotImage(int workersThreads, int maxIterations, double maxBetrag, int yStart, int yStop, int xStart, int xStop, int xpix, int ypix, double xMinimum, double xMaximum, double yMinimum, double yMaximum) throws RemoteException {
        WorkerManager worker_manager;
        synchronized (this) {
            worker_manager = searchWorker();
            worker_manager.worker_arbeit_start();
        }
        int[][] colors = worker_manager.worker.calculateMandelbrotImage_worker(workersThreads, maxIterations, maxBetrag, yStart, yStop, xStart, xStop, xpix, ypix, xMinimum, xMaximum, yMinimum, yMaximum);
        worker_manager.worker_arbeit_end();
        return colors;
    }

    public static void main(String[] args) {
        if (args.length == 1){
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                String currentIP = localhost.getHostAddress();
                int masterPort = Integer.parseInt(args[0]);

                String masterUrl = "rmi://" + currentIP + ":" + masterPort + "/MasterServer";

                Master master = new Master();
                LocateRegistry.createRegistry(masterPort);
                Naming.rebind(masterUrl, master);

                System.out.println("Master gestartet...\nURL: " + masterUrl + "\n\n");
            } catch (Exception e) {
                System.err.println("Master exception:");
                e.printStackTrace();
            }
        }else{
            System.out.println("Erforderliche Parameter: <Port>");
        }
    }
}