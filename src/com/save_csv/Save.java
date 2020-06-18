package com.save_csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Save {
	
	 public  void saveCsv(File csv) {
	        String jdbcURL = "jdbc:mysql://localhost:3306/csv";
	        String username = "user";
	        String password = "password";
	 
	        File csvFile = csv;
	 
	        int batchSize = 20;
	 
	        Connection connection = null;
	 
	        try {
	 
	            connection = DriverManager.getConnection(jdbcURL, username, password);
	            connection.setAutoCommit(false);
	 
	            String sql = "INSERT INTO review (result_time, granularity_period, object_name, cell_id, callattemps) VALUES (?, ?, ?, ?, ?)";
	            PreparedStatement statement = connection.prepareStatement(sql);
	 
	            BufferedReader lineReader = new BufferedReader(new FileReader(csvFile));
	            String lineText = null;
	 
	            int count = 0;
	 
	            lineReader.readLine(); 
	 
	            while ((lineText = lineReader.readLine()) != null) {
	                String[] data = lineText.split(",");
	                String result_time = data[0];
	                String granularity_period = data[1];
	                String object_name = data[2];
	                String cell_id = data[3];
	                String callattemps = data[4];
	 
	                statement.setString(1, result_time);
	                statement.setString(2, granularity_period);
	                statement.setString(3, object_name);
	                statement.setString(4, cell_id);
	                statement.setString(5, callattemps);
	                
	                statement.addBatch();
	 
	                if (count % batchSize == 0) {
	                    statement.executeBatch();
	                }
	            }
	 
	            lineReader.close();
	 
	            // execute the remaining queries
	            statement.executeBatch();
	 
	            connection.commit();
	            connection.close();
	 
	        } catch (IOException ex) {
	            System.err.println(ex);
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	 
	            try {
	                connection.rollback();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	 
	    }
}
