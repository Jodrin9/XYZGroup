/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author namso1902
 */
public class Member {

    //class parameters (first commit)    
    private String id;
    private String name;
    private String address;
    private Date dob;
    private Date dor;
    String status; //provisional (p), full (f), suspended (s) etc.
    boolean paymentMade;
    PreparedStatement pstmt;
    //Username and Password
    String username, password;
    
    public Member(String id, String name, String address, Date dob) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.dob = dob;
        this.dor = MemberManager.getCurrentDate();
        this.status = "APPLIED";
        this.password = MemberManager.generatePassword(dob);
    }
    
    //add member to db (member table)
    public static void addMember(String id, String name, String address, 
            Date dob) throws SQLException {
        Jdbc dbConn = new Jdbc();
        Connection con = dbConn.connect();
        pstmt = con.prepareStatement("INSERT INTO members VALUES(?,?,?,?,?,?,?)");
        pstmt.setString(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, address);
        pstmt.setDate(4, new java.sql.Date(dob.getTime()));
        pstmt.setDate(5, new java.sql.Date(dor.getTime()));
        pstmt.setString(6, status);
        pstmt.executeQuery();
        con.close();
    }
    
    //subract payment from balance
    public static void updateBalance(String userId, float addAmount) 
            throws SQLException {
        //Get current balance and add in payment. Then update with new balance. 
        Jdbc dbConn = new Jdbc();
        Connection con = dbConn.connect();
        String sql_get_balance = "SELECT balance FROM members WHERE id = " + 
                userId;
        float balance = dbConn.executeQuery(sql_get_balance).
                getFloat("balance");
        balance += addAmount;
        String sql_update_balance = "UPDATE members SET balance = " + balance  
                + " WHERE id = " + userId;
        dbConn.executeQuery(sql_update_balance);
        dbConn.close();
        //return balance;
    }
    
    public static void makePayment(String userId, String paymentType, float amount, Date d) throws SQLException {
        Jdbc dbConn = new Jdbc();
        Connection con = dbConn.connect();
        PreparedStatement pstmt;
        pstmt = con.prepareStatement("INSERT INTO payments VALUES(?,?,?,?)");
        pstmt.setString(2, userId);
        pstmt.setString(3, paymentType);
        pstmt.setFloat(4, amount);
        pstmt.setDate(5, new java.sql.Date(d.getTime()));
        //update balance
        updateBalance(userId, amount);
        pstmt.executeQuery();
        con.close();
    }
    
    public static void submitClaim(String userId, Date d, String rationale, float claimAmount) throws SQLException {
        //insert new claim into db (check claim can be made)
        Jdbc dbConn = new Jdbc();
        Connection con = dbConn.connect();
        PreparedStatement pstmt;
        pstmt = con.prepareStatement("INSERT INTO claims VALUES(?,?,?,?,?,?,?)");
        pstmt.setString(1, userId);
        pstmt.setDate(2, new java.sql.Date(d.getTime()));
        pstmt.setString(3, rationale);
        pstmt.setString(4, "SUBMITTED");
        pstmt.setFloat(5, claimAmount);
        pstmt.executeQuery();
        dbConn.close();
    }
    
    public static ResultSet getAllMemberClaims(String userId) {
        Jdbc dbConn = new Jdbc();
        Connection con = dbConn.connect();
        ResultSet claims = null; 
        String sql_get_claims = "SELECT * FROM claims WHERE id = " + userId;
        try {
            claims = dbConn.executeQuery(sql_get_claims);
            dbConn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Member.class.getName()).log(Level.SEVERE, null, ex);
        }
        return claims;
    }
    
    public static ResultSet getAllPayments(String userId) {
        Jdbc dbConn = new Jdbc();
        Connection con = dbConn.connect();
        ResultSet payments = null;
        String sql_get_payments  = "SELECT * FROM payments WHERE id = " + userId;
        try {
           payments = dbConn.executeQuery(sql_get_payments);
           dbConn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Member.class.getName()).log(Level.SEVERE, null, ex);
        }
        return payments;
    }

}
