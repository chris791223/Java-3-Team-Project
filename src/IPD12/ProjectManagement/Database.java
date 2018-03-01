/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.Action;

/**
 *
 * @author 1796111
 */
public class Database {
    private final static String HOSTNAME = "den1.mysql3.gear.host:3306";
    private final static String DBNAME = "pjms";
    private final static String USERNAME = "pjms";
    private final static String PASSWORD = "Bh1x~QbvBu!x";

    private Connection conn;
    
    public Database() throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:mysql://" + HOSTNAME + "/" + DBNAME + "?useSSL=false",
                USERNAME, PASSWORD);  
    }
    
    public ArrayList<Team> getAllTeamMembers(long projectId) throws SQLException {
        
        String sql = "SELECT u.id, u.name, u.ability FROM teams AS t join users AS u on t.userId = u.id WHERE t.projectId = ? AND t.isLeft = 0";
        ArrayList<Team> list = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            
            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                long id = result.getLong("id");
                String name = result.getString("name");
                String ability = result.getString("ability");
                
                Team teamMember = new Team(id, name, ability);
                list.add(teamMember);
            }
        } 
        
        return list;
    }
    
    public ArrayList<Team> getAllTeamAvailabeResouces() throws SQLException {
        
        String sql = "SELECT id, name, ability FROM users WHERE isAvailable = 1";
        ArrayList<Team> list = new ArrayList<>();
     
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(sql);
            
            while (result.next()) {
                long id = result.getLong("id");
                String name = result.getString("name");
                String ability = result.getString("ability");
                
                Team availableResource = new Team(id, name, ability);
                list.add(availableResource);
            }
        } 
        
        return list;
    }
    
    /*
    public Car getCarById(long id) throws SQLException{
        String sql = "SELECT * FROM cars WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return new Car(result.getLong("id"), result.getString("makeModel"), result.getBigDecimal("engineSize"), Car.FuelType.valueOf(result.getString("fuelType")));
            } else {
                return null;
            }
        }
    }
   
    public ArrayList<Car> getAllCars() throws SQLException {
        String sql = "SELECT * FROM cars";
        ArrayList<Car> list = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(sql);
            while (result.next()) {
                long id = result.getLong("id");
                String makeModel = result.getString("makeModel");
                BigDecimal engineSize = result.getBigDecimal("engineSize");
                Car.FuelType fuelType = Car.FuelType.valueOf(result.getString("fuelType"));
                
                Car car = new Car(id, makeModel, engineSize, fuelType);
                list.add(car);
            }
        }
        return list;
    }
    
    public void addCar(Car car) throws SQLException {
        String sql = "INSERT INTO cars (makeModel, engineSize, fuelType) VALUES(?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, car.getMakeModel());
            //stmt.setDouble(2, 2.4);
            stmt.setDouble(2, car.getEngineSize().doubleValue());
            stmt.setString(3, car.getFuelType().toString());
            
            stmt.executeUpdate();
        } 
    }
    
    
    public void updateCar(Car car) throws SQLException {
        String sql = "UPDATE cars SET makeModel = ?, engineSize = ?, fuelType = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, car.getMakeModel());
            stmt.setDouble(2, car.getEngineSize().doubleValue());
            stmt.setString(3, car.getFuelType().toString());
            stmt.setLong(4, car.getId());
            
            stmt.executeUpdate();
        }
    }
    
    public void deleteCar(long id) throws SQLException {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            stmt.executeUpdate();
        }
    }
 */   
    
   // For Jerry
   ////////////////////////////////////////////////////////////////////
   
    public String getPasswordByEmail(String email) throws SQLException{
        String sql = "SELECT password FROM users WHERE email = ?";                
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("password");
            }
        }
        return "";
    }
    public String getPasswordByEmployeeID(String ID) throws SQLException{
        String sql = "SELECT password FROM users WHERE id =" + ID;                
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("password");
            }
        }
        return "";
    } 
    
    
   ////////////////////////////////////////////////////////////////////
    /**
     * @param conn the conn to set
     */
    public void setAutoCommit(boolean flag) throws SQLException {
        conn.setAutoCommit(flag); 
    }
    
    public void commitUpdate() throws SQLException {
        conn.commit();
    }

    public void rollbackUpdate() throws SQLException {
        conn.rollback();
    }
    
    
    
}
