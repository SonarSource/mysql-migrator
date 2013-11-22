package diagramme;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 4:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class DebugTableContent {

    private SonarBDD sonarBDD;

    public DebugTableContent(SonarBDD bdd){
        this.sonarBDD = bdd;
    }

    public void affichage(){
        for(int indexTable=0; indexTable<sonarBDD.getBDDTables().size();indexTable++){
            System.out.println("*** TABLE : "+sonarBDD.getBDDTables().get(indexTable).getTableName());
            for(int indexColumn=0; indexColumn<sonarBDD.getBDDTables().get(indexTable).getColumns().size();indexColumn++){
                System.out.println("****** COLUMN : "+sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getColumnName());
                for(int indexData=0;indexData<sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getDataList().size();indexData++){
                    System.out.print(sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getDataList().get(indexData)+" : ");
                    System.out.println();
                }
            }
        }
    }

    public void AfficheColumnsAndRows()
    {

        for(int indexTable=0; indexTable<sonarBDD.getBDDTables().size();indexTable++){
            System.out.println("*** TABLE : "+sonarBDD.getBDDTables().get(indexTable).getTableName()+"--- NB ROWS = "+sonarBDD.getBDDTables().get(indexTable).getNbRows());
            for(int indexColumn=0; indexColumn<sonarBDD.getBDDTables().get(indexTable).getColumns().size();indexColumn++){
                System.out.println("****** COLUMN : "+sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getColumnName()+" -> TYPE = "+sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getColumnType());
            }
        }
    }
}
