package pt.ulisboa.tecnico.sec.g19.hdscoin.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String SUFIX = "_hdscoin";
    private String databaseName;
    public Database(String name) {
        databaseName = name + SUFIX + ".db";
    }

    public Connection getConnection() throws SQLException {
        Connection conn=  DriverManager.getConnection("jdbc:sqlite:" + databaseName);
        // we want explicit transactions and commits to avoid inconsistent states
        conn.setAutoCommit(false);
        return conn;
    }

    public void recreateSchema() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement statement = conn.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists tx");
            statement.executeUpdate("drop table if exists ledger");

            statement.executeUpdate("create table ledger (" +
                    "id integer primary key, " +
                    "public_key text not null, " +
                    "balance integer not null, " +
                    "timestamp integer not null)");

            statement.executeUpdate("create table tx (" + // "transaction" is a reserved SQLite keyword
                    "id integer primary key, " +
                    "ledger_id integer not null, " +
                    "other_id integer not null, " +
                    "is_send integer not null, " + // sqlite does not support booleans
                    "amount integer not null, " +
                    "nonce text not null, " +
                    "hash text not null, " +
                    "prev_hash text, " + // can be null (first transaction)
                    "pending integer not null, " +
                    "foreign key (ledger_id) references ledger(id), " +
                    "foreign key (other_id) references ledger(id), " +
                    "foreign key (prev_hash) references tx(hash))"); // sqlite does not support booleans

            conn.commit();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
