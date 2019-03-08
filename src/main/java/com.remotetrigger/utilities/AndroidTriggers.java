package main.java.com.remotetrigger.utilities;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;

public class AndroidTriggers {

    public Connection connect() throws Exception {

        Connection connection = null;
        try {

            Class.forName("org.postgresql.Driver");
//                connection = DriverManager.getConnection("jdbc:postgresql://0.tcp.ngrok.io:18959/PaymentTiming", "sajeel", "sajeel");
            connection = DriverManager.getConnection("jdbc:postgresql://0.tcp.ngrok.io:18838/RebootSajeel", "sajeel", "sajeel");

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
        return connection;
    }

    void closedb(Connection con) throws Exception {

        try {
            con.close();
        } catch (Exception e) {
            //Do nothing
        }
    }

    public ArrayList<String> getTriggers() throws Exception {
        String sql = "select \"RECOGNIZED_SPEECH\", \"TIMESTAMP\" from \"Speech\" where \"PROCESSED\" = false;";
        Connection conn = connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        ArrayList<String> commands = new ArrayList<>();

        while (rs.next()) {
            String raw = "";
            raw = rs.getString(1);
            Long big  = (Long) rs.getObject(2);
            raw = raw.concat(";").concat(big.toString());
            commands.add(raw);
        }

        closedb(conn);
        return commands;
    }


    public void updateSpeech(String timeStamp) throws Exception {

        Connection con = connect();
        con.setAutoCommit(true);

        String query = "update \"Speech\" set \"PROCESSED\" = true where \"TIMESTAMP\" = '" + timeStamp + "';";
        PreparedStatement ps = con.prepareStatement(query);
        ps.execute();
        System.out.println(ps.toString());
        closedb(con);
    }


    public static void main(String[] args) throws Exception {

        while (true)
        {
            AndroidTriggers androidTriggers = new AndroidTriggers();
            ArrayList<String> commands = androidTriggers.getTriggers();
            for(int i = 0; i < commands.size(); i++)
            {
                switch (commands.get(i).split(";")[0])
                {
                    case "Trigger smoke suite" :
                    {
                        System.out.println("Executing Smoke Suite");
                        new ExecCommand("gradle clean build -PSmoke");
                        androidTriggers.updateSpeech(commands.get(i).split(";")[1]);
                        System.out.println("Executed Successfully !!");
                    }
                }
            }
        }


    }

}