package me.avery246813579.universalcredits.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.avery246813579.universalcredits.UniversalCredits;

public class MoneyLoad {
	private Connection con;
    private UniversalCredits plugin;

    public MoneyLoad(UniversalCredits MoneySystem) {
        this.plugin = MoneySystem;
    }

    public void SQLdisconnect() {
        try {
            plugin.getLogger().info("Disconnecting from MySQL database...");
            this.con.close();
        } catch (SQLException ex) {
            plugin.getLogger().severe("Error while closing the connection...");
        } catch (NullPointerException ex) {
            plugin.getLogger().severe("Error while closing the connection...");
        }
    }

    public void SQLconnect() {
        try {
            plugin.getLogger().info("Connecting to MySQL database...");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String conn = "jdbc:mysql://" + this.plugin.getSQL_HOST() + "/" + this.plugin.getSQL_DATA();
            this.con = DriverManager.getConnection(conn, this.plugin.getSQL_USER(), this.plugin.getSQL_PASS());
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().severe("No MySQL driver found!");
        } catch (SQLException ex) {
            plugin.getLogger().severe("Error while fetching MySQL connection!");
        } catch (Exception ex) {
            plugin.getLogger().severe("Unknown error while fetchting MySQL connection.");
        }

        boolean exists = true;
        try {
            DatabaseMetaData dbm = this.con.getMetaData();

            ResultSet tables = dbm.getTables(null, null, "UniversalCredits", null);
            if (!tables.next()) exists = false;
        } catch (SQLException localSQLException1) {
        } catch (NullPointerException localNullPointerException1) {
        }
        if (!exists) {
            String sta = "CREATE TABLE UniversalCredits (ID int(10) unsigned NOT NULL AUTO_INCREMENT, Name varchar(20) NOT NULL, Money int(20) NOT NULL, PRIMARY KEY (`ID`))";
            try {
                Statement st = this.con.createStatement();
                st.executeUpdate(sta);
                st.close();
            } catch (SQLException ex) {
                plugin.getLogger().severe("Error with following query: " + sta);
                plugin.getLogger().severe("MySQL-Error: " + ex.getMessage());
            } catch (NullPointerException ex) {
                plugin.getLogger().severe("Error while performing a query. (NullPointerException)");
            }
        }
    }

    public void refresh(String player) {
        try {
            Statement stmt = this.con.createStatement();
            ResultSet r = stmt.executeQuery("SELECT * FROM `UniversalCredits` WHERE `Name` = '" + player + "' ;");
            r.last();
            if (r.getRow() == 0) {
                stmt.close();
                r.close();
                plugin.getLogger().info(player + "'s money doesn't exist, Creating it");
                String insert = "INSERT INTO UniversalCredits (Name, Money, Passes) VALUES ('" + player + "', '100', '0')";
                try {
                    Statement stamt = this.con.createStatement();
                    stamt.executeUpdate(insert);
                    stamt.close();
                    refresh(player);
                } catch (SQLException ex) {
                    plugin.getLogger().severe("MySql error while creating new balance for " + player + ", Error: " + ex);
                } catch (NullPointerException ex) {
                    plugin.getLogger().severe("MySql error while creating new balance for " + player + ", Error: " + ex);
                }
            } else {
                plugin.getBalance().put(player, r.getInt("Money"));
                stmt.close();
                r.close();
            }
        } catch (SQLException ex) {
            plugin.getLogger().severe("Error while fetching " + player + "'s Money: " + ex);
        } catch (NullPointerException ex) {
            plugin.getLogger().severe("Error while fetching " + player + "'s Money: " + ex);
            ex.printStackTrace();
        }
    }
}
