package com.rrinnne.intergation.apiRoutes;
import com.rrinnne.intergation.apiRoutes.constantApi.Api;
import com.rrinnne.intergation.dataBase.DataBase;
import com.rrinnne.intergation.models.Spare;
import org.apache.camel.builder.RouteBuilder;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CsvApiRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("timer:generateCsv?period=300000")

                .process(exchange -> {
                    DataSource source = DataBase.getDataSource();
                    List<Spare> spareList = new ArrayList<>();

                    try (Connection connection = source.getConnection();
                         PreparedStatement statement = connection.prepareStatement(
                                 "SELECT sparecode, sparename, sparedescription, sparetype, sparestatus, price, quantity, updatedat FROM spares"
                         );
                         ResultSet results = statement.executeQuery()) {

                        while (results.next()) {
                            Spare item = new Spare();
                            item.setSpareCode(results.getString("sparecode"));
                            item.setSpareName(results.getString("sparename"));
                            item.setSpareDescription(results.getString("sparedescription"));
                            item.setSpareType(results.getString("sparetype"));
                            item.setSpareStatus(results.getString("sparestatus"));
                            item.setPrice(results.getBigDecimal("price"));
                            item.setQuantity(results.getInt("quantity"));
                            item.setUpdatedAt(results.getString("updatedat"));
                            spareList.add(item);
                        }
                    }

                    String tempPath = System.getProperty("java.io.tmpdir") + File.separator + "spares_export.csv";
                    File csvFile = new File(tempPath);

                    if (!csvFile.getParentFile().exists()) {
                        csvFile.getParentFile().mkdirs();
                    }

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                        for (Spare s : spareList) {
                            String price = s.getPrice().stripTrailingZeros().toPlainString();
                            String row = String.join(";",
                                    s.getSpareCode(),
                                    s.getSpareName(),
                                    s.getSpareDescription(),
                                    s.getSpareType(),
                                    s.getSpareStatus(),
                                    price,
                                    String.valueOf(s.getQuantity()),
                                    s.getUpdatedAt()
                            );
                            writer.write(row);
                            writer.newLine();
                        }
                    }

                    exchange.getIn().setHeader("filePathCsv", tempPath);
                })

                .setHeader("Content-Type", constant("text/csv; charset=UTF-8"))
                .setHeader("CamelHttpMethod", constant("POST"))
                .process(exchange -> {
                    String path = exchange.getIn().getHeader("filePathCsv", String.class);
                    exchange.getIn().setBody(new File(path));
                })

                .toD(Api.apiRoot + "/students/4/report/csv");
    }
}
