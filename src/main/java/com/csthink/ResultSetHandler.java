package com.csthink;


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 函数式接口
 */
@FunctionalInterface
public interface ResultSetHandler {

    /**
     * This method will be executed by the lambda expression
     *
     * @param resultSet
     * @throws SQLException
     */
    public void handle(ResultSet resultSet) throws SQLException;

}
