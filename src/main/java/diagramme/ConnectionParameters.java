package diagramme;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/20/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionParameters {

    public int bdd, port;
    private String target;

    public ConnectionParameters(String target){
            this.target=target;
            getBdd();
            getPort();
    }

    public void getBdd(){
        Scanner scanner = new Scanner(System.in);
        bdd=0;

        while(bdd<1 || bdd>5){
            System.out.println("Veuillez saisir la base "+target+" : ");
            System.out.println("1- ORACLE");
            System.out.println("2- POSTGRESQL");
            System.out.println("3- MYSQL");
            System.out.println("4- SQLSERVER");

            bdd = scanner.nextInt();
        }
    }

    public void getPort (){
        Scanner scanner = new Scanner(System.in);
        port = 0;
        while (port<1024 || port>65535){
            System.out.println("Veuillez saisir le port "+target+": ");
            port = scanner.nextInt();
        }
    }
}
