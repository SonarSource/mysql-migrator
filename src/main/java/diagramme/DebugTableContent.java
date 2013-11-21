package diagramme;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 4:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class DebugTableContent {

    public DebugTableContent(SonarBDD bdd){
         System.out.println("in debug");
        SonarBDD sonarBDD = bdd;

        for(int indexTable=0; indexTable<sonarBDD.tables_of_bdd.size();indexTable++){
            System.out.println("*** TABLE: "+sonarBDD.tables_of_bdd.get(indexTable).tableName);
            for(int indexColumn=0; indexColumn<sonarBDD.tables_of_bdd.get(indexTable).columns.size();indexColumn++){
                System.out.print(" : "+sonarBDD.tables_of_bdd.get(indexTable).columns.get(indexColumn).columnName);
                for(int indexData=0;indexData<sonarBDD.tables_of_bdd.get(indexTable).columns.get(indexColumn).objectTableData.size();indexData++){
                    System.out.print(" : "+sonarBDD.tables_of_bdd.get(indexTable).columns.get(indexColumn).objectTableData.get(indexData));
                }
            }
        }
    }
}
