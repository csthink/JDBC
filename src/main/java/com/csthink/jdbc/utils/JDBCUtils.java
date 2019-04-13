package com.csthink.jdbc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtils {

    private static final String DRIVER_CLASS;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    static {
        // 加载属性文件并解析：
        Properties props = new Properties();
        // 使用类的加载器的方式获取属性文件的输入流
        InputStream is = JDBCUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");

        try {
            props.load(is);
        } catch (IOException e) {
            System.out.println("数据库配置文件 jdbc.properties 读取失败");
        }

        DRIVER_CLASS = props.getProperty("DRIVER_CLASS");
        URL = props.getProperty("URL");
        USERNAME = props.getProperty("USERNAME");
        PASSWORD = props.getProperty("PASSWORD");
    }

    /**
     * 注册驱动
     */
    private static void loadDriver() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.out.println("驱动类" + DRIVER_CLASS + "加载失败");
        }
    }

    /**
     * 建立数据库连接
     *
     * @return Connection 数据库连接
     */
    public static Connection getConnection() throws SQLException {
        // 得到当前线程上绑定的连接
        Connection connection = threadLocal.get();
        // 如果有事务，返回当前事务的连接
        if (connection != null) {
            return connection;
        }

        // 如果没有事务，创建新的链接
        loadDriver(); // 加载驱动

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }


    /**
     * 开启事务
     */
    public static void startTransaction() throws SQLException {
        Connection connection = threadLocal.get(); // 获取当前线程的事务连接
        if (connection != null) {
            throw new SQLException("事务已开启，不能重复开启");
        }

        loadDriver(); // 加载驱动
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        connection.setAutoCommit(false); // 设置手动提交
        threadLocal.set(connection); // 把当前事务连接放到线程中
    }

    /**
     * 提交事务
     *
     * @throws SQLException
     */
    public static void commit() throws SQLException {
        Connection connection = threadLocal.get(); // 获取当前线程的事务连接
        if (connection == null) {
            throw new SQLException("没有事务不能提交！");
        }

        connection.commit();
    }

    /**
     * 回滚事务
     *
     * @throws SQLException
     */
    public static void rollback() throws SQLException {
        Connection connection = threadLocal.get(); // 获取当前线程的事务连接
        if (connection == null) {
            throw new SQLException("没有事务不能提交！");
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
