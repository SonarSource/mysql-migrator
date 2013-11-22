package diagramme;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/22/13
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListColumnsAsString {

    private List<SonarColumn> columnList;
    private String columnsAsString, questionMarkString;
    private int nbColumns;

    public ListColumnsAsString(List<SonarColumn> list){
        columnList = list;
        nbColumns = columnList.size();

    }

    public String makeString(){
        columnsAsString = new String();
        for(int indexColumn=0;indexColumn<nbColumns;indexColumn++){
            columnsAsString+=","+columnList.get(indexColumn).getColumnName();
        }
        columnsAsString = columnsAsString.substring(1);
        return columnsAsString;
    }

    public String makeQuestionMarkString(){
        questionMarkString ="?";
        for(int indexColumn=0;indexColumn<nbColumns-1;indexColumn++){
            questionMarkString+=",?";
        }
        return questionMarkString;
    }
}
