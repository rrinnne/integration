package com.rrinnne.intergation;
import com.rrinnne.intergation.apiRoutes.CmsApiRoute;
import com.rrinnne.intergation.apiRoutes.CsvApiRoute;
import com.rrinnne.intergation.dataBase.DataBase;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import javax.sql.DataSource;

public class MainApp {
    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        DataSource ds = DataBase.getDataSource();
        context.getRegistry().bind("myDataSource", ds);

        context.addRoutes(new CmsApiRoute());
        context.addRoutes(new CsvApiRoute());

        context.start();
        System.out.println("Приложение запущено!");

        Thread.sleep(Long.MAX_VALUE);
    }
}
