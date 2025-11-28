package com.rrinnne.intergation.apiRoutes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rrinnne.intergation.apiRoutes.constantApi.Api;
import com.rrinnne.intergation.dataBase.DataBase;
import com.rrinnne.intergation.models.Spare;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CmsApiRoute extends RouteBuilder {

    @Override
    public void configure() {

        from("timer:syncCms?period=60000")
                .setHeader("currentPage", constant(0))
                .setHeader("pageSize", constant(10))
                .setProperty("continueFetching", constant(true))

                .loopDoWhile(simple("${exchangeProperty.continueFetching} == true"))

                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD(Api.apiRoot + "/students/4/cms/spares"
                        + "?page=${header.currentPage}&size=${header.pageSize}&bridgeEndpoint=true")

                .process(exchange -> {
                    String response = exchange.getIn().getBody(String.class);

                    if (response.isEmpty() || response.equals("[]") || response.contains("\"content\":[]") || response.contains("\"content\": []")) {
                        exchange.setProperty("continueFetching", false);
                        return;
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

                    List<Spare> items = mapper.readValue(response, new TypeReference<List<Spare>>() {});

                    DataSource dataSource = DataBase.getDataSource();
                    try (Connection connection = dataSource.getConnection()) {

                        Set<String> currentCodes = new HashSet<>();

                        for (Spare item : items) {
                            currentCodes.add(item.getSpareCode());

                            PreparedStatement checkStmt = connection.prepareStatement(
                                    "SELECT 1 FROM spares WHERE sparecode = ?"
                            );
                            checkStmt.setString(1, item.getSpareCode());
                            ResultSet rs = checkStmt.executeQuery();
                            boolean exists = rs.next();
                            rs.close();
                            checkStmt.close();

                            if (exists) {
                                PreparedStatement updateStmt = connection.prepareStatement(
                                        "UPDATE spares SET sparename=?, sparedescription=?, sparetype=?, sparestatus=?, price=?, quantity=?, updatedat=?, isdeleted=false WHERE sparecode=?"
                                );
                                updateStmt.setString(1, item.getSpareName());
                                updateStmt.setString(2, item.getSpareDescription());
                                updateStmt.setString(3, item.getSpareType());
                                updateStmt.setString(4, item.getSpareStatus());
                                updateStmt.setBigDecimal(5, item.getPrice());
                                updateStmt.setInt(6, item.getQuantity());
                                updateStmt.setTimestamp(7, Timestamp.valueOf(item.getUpdatedAt()));
                                updateStmt.setString(8, item.getSpareCode());
                                updateStmt.executeUpdate();
                                updateStmt.close();
                            } else {
                                PreparedStatement insertStmt = connection.prepareStatement(
                                        "INSERT INTO spares(sparecode, sparename, sparedescription, sparetype, sparestatus, price, quantity, updatedat, isdeleted) VALUES (?,?,?,?,?,?,?,?,false)"
                                );
                                insertStmt.setString(1, item.getSpareCode());
                                insertStmt.setString(2, item.getSpareName());
                                insertStmt.setString(3, item.getSpareDescription());
                                insertStmt.setString(4, item.getSpareType());
                                insertStmt.setString(5, item.getSpareStatus());
                                insertStmt.setBigDecimal(6, item.getPrice());
                                insertStmt.setInt(7, item.getQuantity());
                                insertStmt.setTimestamp(8, Timestamp.valueOf(item.getUpdatedAt()));
                                insertStmt.executeUpdate();
                                insertStmt.close();
                            }
                        }

                        if (!currentCodes.isEmpty()) {
                            String placeholders = currentCodes.stream().map(c -> "?").collect(Collectors.joining(","));
                            PreparedStatement markDeleted = connection.prepareStatement(
                                    "UPDATE spares SET isdeleted=true WHERE sparecode NOT IN (" + placeholders + ")"
                            );
                            int index = 1;
                            for (String code : currentCodes) {
                                markDeleted.setString(index++, code);
                            }
                            markDeleted.executeUpdate();
                            markDeleted.close();
                        }
                    }
                })

                .process(exchange -> {
                    int nextPage = exchange.getIn().getHeader("currentPage", Integer.class) + 1;
                    exchange.getIn().setHeader("currentPage", nextPage);
                })

                .end();
    }
}
