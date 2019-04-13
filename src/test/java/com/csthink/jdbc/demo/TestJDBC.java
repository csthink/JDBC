package com.csthink.jdbc.demo;

import com.csthink.ResultSetHandler;
import com.csthink.domain.Employee;
import com.csthink.jdbc.utils.JDBCUtils;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TestJDBC {

    /**
     * 获取一条记录
     */
    @Test
    public void test1() {
        Connection connect = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        try {
            // 获取连接
            connect = JDBCUtils.getConnection();
            String sql = "SELECT * FROM employee WHERE username = ? AND password = ?";
            // 获取预处理sql对象
            ps = connect.prepareStatement(sql);
            // 设置参数
            ps.setString(1, "aaa");
            ps.setString(2, "111");
            // 执行sql
            resultSet = ps.executeQuery();
            // 判断结果集
            if (resultSet.next()) {
                Employee employee = new Employee();
                employee.setUid(resultSet.getInt(1));
                employee.setUsername(resultSet.getString(2));
                employee.setPassword(resultSet.getString(3));
                employee.setName(resultSet.getString(4));
                employee.setCreateTime(resultSet.getDate(5));

                System.out.println(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            JDBCUtils.close(ps);
            JDBCUtils.close(resultSet);
            JDBCUtils.close(connect);
        }
    }

    /**
     * 使用事务新增一条记录
     */
    @Test
    public void test2() {
        Connection connect = null;
        PreparedStatement ps = null;

        try {
            // 开启事务
            JDBCUtils.startTransaction();
            connect = JDBCUtils.getConnection();
            // 获取连接
            String sql = "INSERT INTO employee(`username`,`password`,`name`,`create_time`) VALUES(?,?,?,?)";
            // 获取预处理sql对象
            ps = connect.prepareStatement(sql);
            // 设置参数
            ps.setString(1, "ruby");
            ps.setString(2, "ruby111");
            ps.setString(3, "ruby大叔");
            Timestamp timestamp = new Timestamp(new Date().getTime());
            ps.setTimestamp(4, timestamp);
            // 执行sql
            ps.executeUpdate();
            // 提交事务
            JDBCUtils.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            JDBCUtils.close(ps);
            JDBCUtils.close(connect);
        }
    }

    /**
     * 获取全部结果集
     */
    @Test
    public void test3() {
        Connection connect = null;
        Statement statement = null;
        ResultSet resultSet = null;

        List<Employee> list = new ArrayList<>();

        try {
            // 获取连接
            connect = JDBCUtils.getConnection();
            String sql = "SELECT * FROM employee";
            // 获取预处理sql对象
            statement = connect.createStatement();
            // 执行sql
            resultSet = statement.executeQuery(sql);
            // 判断结果集
            while (resultSet.next()) {
                Employee employee = new Employee();
                employee.setUid(resultSet.getInt(1));
                employee.setUsername(resultSet.getString(2));
                employee.setPassword(resultSet.getString(3));
                employee.setName(resultSet.getString(4));
                employee.setCreateTime(resultSet.getDate(5));
                list.add(employee);
            }

            System.out.println(list);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            JDBCUtils.close(statement);
            JDBCUtils.close(resultSet);
            JDBCUtils.close(connect);
        }
    }

    /**
     * 批处理，使用 PreparedStatement
     */
    @Test
    public void test4() {
        Connection connect = null;
        PreparedStatement ps = null;

        try {
            connect = JDBCUtils.getConnection();
            // 获取连接
            String sql = "UPDATE employee SET `username` = ? WHERE `name` = ?";
            // 获取预处理sql对象
            ps = connect.prepareStatement(sql);

            // 设置批处理参数
            ps.setObject(1, "ruby2");
            ps.setObject(2, "ruby大叔");
            // 添加批处理
            ps.addBatch();

            // 设置批处理参数
            ps.setObject(1, "go2");
            ps.setObject(2, "go大叔");
            // 添加批处理
            ps.addBatch();

            // 执行所有的批处理
            int[] updatedRecords = ps.executeBatch();
            // 统计执行结果
            int total = 0;
            for (int recordUpdated : updatedRecords) {
                total += recordUpdated;
            }
            System.out.println("批处理更新记录数: " + total);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            JDBCUtils.close(ps);
            JDBCUtils.close(connect);
        }
    }

    /**
     * 批处理,使用 Statement
     */
    @Test
    public void test5() {
        Connection connect = null;
        Statement statement = null;

        try {
            connect = JDBCUtils.getConnection(); // 获取连接
            statement = connect.createStatement(); // 获取预处理sql对象
            statement.addBatch("UPDATE employee SET `username` = 'ruby3' WHERE `name` = 'ruby大叔'");
            statement.addBatch("UPDATE employee SET `username` = 'go3' WHERE `name` = 'go大叔'");
            // 执行所有的批处理
            int[] updatedRecords = statement.executeBatch();

            // 统计执行结果
            int total = 0;
            for (int recordUpdated : updatedRecords) {
                total += recordUpdated;
            }
            System.out.println("批处理更新记录数: " + total);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            JDBCUtils.close(statement);
            JDBCUtils.close(connect);
        }
    }

    /**
     * 使用 JAVA 8 的新特性
     */
    @Test
    public void test6() {
        Connection connect = null;

        List<Employee> list = new ArrayList<>();

        try {
            // 获取连接
            connect = JDBCUtils.getConnection();
            String sql = "SELECT * FROM employee";

            select(connect, "SELECT * FROM employee", (resultSet) -> {
                Employee employee = new Employee();
                employee.setUid(resultSet.getInt(1));
                employee.setUsername(resultSet.getString(2));
                employee.setPassword(resultSet.getString(3));
                employee.setName(resultSet.getString(4));
                employee.setCreateTime(resultSet.getDate(5));
                list.add(employee);
            });

            System.out.println(list);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            JDBCUtils.close(connect);
        }
    }

    /**
     * 查询方法
     *
     * @param connect Connection对象
     * @param sql     SQL查询语句
     * @param handler Lambda表达式 --> 接收一个参数(ResultSet的实例)
     * @throws SQLException 异常
     */
    public static void select(Connection connect, String sql, ResultSetHandler handler) throws SQLException {
        PreparedStatement statement = connect.prepareStatement(sql);

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                handler.handle(resultSet);
            }

        }
    }
}
