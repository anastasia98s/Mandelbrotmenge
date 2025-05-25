import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject {

    Client(MasterInterface master) throws RemoteException {
        ClientPresenter p = new ClientPresenter();
        ClientView v = new ClientView(p);
        ClientModel m = new ClientModel(p, master);
        p.init(m, v);
    }

    public static void main(String[] args) {
        if (args.length == 2){
            try {
                String masterIP = args[0];
                int masterPort = Integer.parseInt(args[1]);

                MasterInterface master = (MasterInterface) java.rmi.registry.LocateRegistry.getRegistry(masterIP, masterPort).lookup("MasterServer");
                new Client(master);

                System.out.println("Verbindung zum Master-Port: " + masterPort + " erfolgreich hergestellt\n\n");
            } catch (Exception e) {
                System.err.println("Fehler beim Starten des Clients");
                e.printStackTrace();
            }
        }else{
            System.out.println("Bitte geben Sie die Master-IP un den Port an!");
            System.out.println("Erforderliche Parameter: <Master IP> <Master Port>");
        }
    }
}