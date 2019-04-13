package com.csthink.jdbc.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0Utils {

    private static DataSource dataSource = new ComboPooledDataSource();

    private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获取连接
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        // 得到当前线程上绑定的连接
        Connection connection = threadLocal.get();
        // 如果有事务，返回当前事务的连接
        if (connection != null) {
            return connection;
        }

        // 如果没有事务，通过连接池返回新的链接
        return dataSource.getConnection();
    }


    /**
     * 开启事务
     *
     * @throws SQLException
     */
    public static void startTransaction() throws SQLException {
        Connection connection = threadLocal.get(); // 获取当前线程的事务连接
        if (connection != null) {
            throw new SQLException("事务已开启，不能重复开启");
        }

        connection = dataSource.getConnection(); // 获取一个连接,表示开启了事务
        connection.setAutoCommit(false); // 设置手动提交
        threadLocal.set(connection); // 把当前事务连接放到线程中
    }

    /**
     * 提交事务,会自动释放资源
     *
     * @throws SQLException
     */
    public static void commit() throws SQLException {
        Connection connection = threadLocal.get(); // 获取当前线程的事务连接
        if (connection == null) {
            throw new SQLException("没有事务不能提交！");
        }
        connection.commit(); // 提交事务
    }

    /**
     * 回滚事务，会自动释放资源
     *
     * @throws SQLException
     */
    public static void rollback() throws SQLException {
        Connection connection = threadLocal.get(); // 获取当前线程的事务连接
        if (connection == null) {
            throw new SQLException("没有事务不能回滚！");
        }
        connection.rollback();
    }

    /**
     * 释放资源
     *
     * @param obj
     */
    public static void close(Object obj) {
        if (null != obj) {
            if (obj instanceof ResultSet) {
                ResultSet rs = (ResultSet) obj;
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println("释放结果集资源出错，" + e.getMessage());
                }
            }

            if (obj instanceof Statement) {
                Statement stmt = (Statement) obj;
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.out.println("释放 statement 资源出错，" + e.getMessage());
                }
            }

            if (obj instanceof Connection) {
                Connection connection = threadLocal.get(); // 获取当前线程的事务连接
                Connection conn = (Connection) obj; // 普通连接

                try {
                    // 如果是事务连接
                    if (connection == conn) {
                        // 解除当前线程上绑定的链接（从 threadLocal 容器中移除对应当前线程的链接）
                        threadLocal.remove();
                    }
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("释放连接资源出错，" + e.getMessage());
                }
            }
        }
    }
}