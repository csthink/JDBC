package com.csthink.jdbc.demo;

import com.csthink.domain.Employee;
import com.csthink.jdbc.utils.C3P0Utils;
import com.csthink.jdbc.utils.JDBCUtils;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestC3P0 {

    /**
     * 获取一条记录
     */
    @Test
    public void test1() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 获取连接
            conn = C3P0Utils.getConnection();
            String sql = "SELECT * FROM employee WHERE username = ? AND password = ?";
            // 获取预处理sql对象
            pstmt = conn.prepareStatement(sql);
            // 设置参数
            pstmt.setString(1, "aaa");
            pstmt.setString(2, "111");
            // 执行sql
            rs = pstmt.executeQuery();
            // 判断结果集
            if (rs.next()) {
                Employee employee = new Employee();
                employee.setUid(rs.getInt(1));
                employee.setUsername(rs.getString(2));
                employee.setPassword(rs.getString(3));
                employee.setName(rs.getString(4));
                employee.setCreateTime(rs.getDate(5));

                System.out.println(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            C3P0Utils.close(pstmt);
            C3P0Utils.close(rs);
            C3P0Utils.close(conn);
        }
    }

    /**
     * 使用事务新增一条记录
     */
    @Test
    public void test2() {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 开启事务
            C3P0Utils.startTransaction();
            conn = C3P0Utils.getConnection();
            // 获取连接
            String sql = "INSERT INTO employee(`username`,`password`,`name`,`create_time`) VALUES(?,?,?,?)";
            // 获取预处理sql对象
            pstmt = conn.prepareStatement(sql);
            // 设置参数
            pstmt.setString(1, "go");
            pstmt.setString(2, "go111");
            pstmt.setString(3, "go大叔");
            Timestamp timestamp = new Timestamp(new Date().getTime());
            pstmt.setTimestamp(4, timestamp);
            // 执行sql
            pstmt.executeUpdate();
            // 提交事务
            C3P0Utils.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            C3P0Utils.close(pstmt);
            C3P0Utils.close(conn);
        }
    }

    /**
     * 获取全部结果集
     */
    @Test
    public void test3() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        List<Employee> list = new ArrayList<>();

        try {
            // 获取连接
            conn = C3P0Utils.getConnection();
            String sql = "SELECT * FROM employee";
            // 获取预处理sql对象
            stmt = conn.createStatement();
            // 执行sql
            rs = stmt.executeQuery(sql);
            // 判断结果集
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setUid(rs.getInt(1));
                employee.setUsername(rs.getString(2));
                employee.setPassword(rs.getString(3));
                employee.setName(rs.getString(4));
                employee.setCreateTime(rs.getDate(5));
                list.add(employee);
            }

            System.out.println(list);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            C3P0Utils.close(stmt);
            C3P0Utils.close(rs);
            C3P0Utils.close(conn);
        }
    }
}
