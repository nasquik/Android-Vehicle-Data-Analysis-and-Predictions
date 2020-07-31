import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/*Used by any class that needs access to the database
* Preconfiguered: call the openConnection function
* and then execute your queries
* */

public class JDBC {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/trafficDatabase";
    static final String USER = "root";
    static final String PASS = "12345678";

    static Connection conn = null;
    static Statement stmt = null;
    static ResultSet rs = null;

    public static void openConnection() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
    }
    public static void insertEntry(String[] entry) throws SQLException {

        System.out.println("Creating statement...");
        stmt = conn.createStatement();
        int entryID = 0;
        rs = null;

        String command = "INSERT INTO trafficData (timestep, device_id,`real_lat`,`real_long`,`predicted_lat`,`predicted_long`,`real_rssi`,`real_throughput`,`predicted_rssi`,`predicted_throughput`) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement pstmt = conn.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);

        pstmt.setString(1, entry[0]);
        pstmt.setString(2, entry[1]);
        pstmt.setString(3, entry[2]);
        pstmt.setString(4, entry[3]);
        pstmt.setString(5, entry[4]);
        pstmt.setString(6, entry[5]);
        pstmt.setString(7, entry[6]);
        pstmt.setString(8, entry[7]);
        pstmt.setString(9, entry[8]);
        pstmt.setString(10, entry[9]);

        int rowAffected = pstmt.executeUpdate();
        System.out.println("Row affected" + rowAffected);
        if(rowAffected == 1)
        {
            rs = pstmt.getGeneratedKeys();
            if(rs.next())
                entryID = rs.getInt(1);
        }
    }
    public static void satisfyQuery(String query) throws SQLException {

        System.out.println("Creating statement...");
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        //STEP 5: Extract data from result set
        while (rs.next()) {
            //Retrieve by column name
            int id = rs.getInt("id");
            int age = rs.getInt("age");
            String first = rs.getString("first");
            String last = rs.getString("last");

            //Display values
            System.out.print("ID: " + id);
            System.out.print(", Age: " + age);
            System.out.print(", First: " + first);
            System.out.println(", Last: " + last);
        }
    }

    public static List<ArrayList<String>> getDB(int vehicleID) throws SQLException {
        List<ArrayList<String>> dbResult = new ArrayList<ArrayList<String>>();

        stmt = conn.createStatement();
        //enter the select query

        String query = "SELECT * FROM trafficData WHERE device_id = "+ vehicleID +" ORDER BY timestep";
        rs = stmt.executeQuery(query);


        while(rs.next()){
            ArrayList<String> list = new ArrayList<String>();

            list.add(rs.getString("device_id"));
            list.add(rs.getString("timestep"));
            list.add(rs.getString("real_long"));
            list.add(rs.getString("real_lat"));
            list.add(rs.getString("predicted_long"));
            list.add(rs.getString("predicted_lat"));
            list.add(rs.getString("real_RSSI"));
            list.add(rs.getString("real_throughput"));
            list.add(rs.getString("predicted_RSSI"));
            list.add(rs.getString("predicted_throughput"));
            dbResult.add(list);

        }

        return dbResult;


    }

    public static void closeConnection() throws SQLException {
        rs.close();
        stmt.close();
        conn.close();
        System.out.println("Goodbye!");
    }
}
