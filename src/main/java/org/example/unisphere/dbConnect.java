package org.example.unishpere;
import java.sql.Connection;
import java.sql.DriverManager;

public class dbConnect {
    public static Connection connection;
    public static Connection getconnection(){
        String dbname="unishpere";
        String username="root";
        String password="";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/"+dbname,username,password);
//            System.out.println("Success");
        }catch (Exception e){
            e.printStackTrace();
//            System.out.println("Failed");
        }
        return connection;
    }

}
