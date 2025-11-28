package com.rrinnne.intergation.dataBase;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DataBase {
    public static DataSource getDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:postgresql://localhost:5432/Spares");
        ds.setUsername("postgres");
        ds.setPassword("1111");
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
}
