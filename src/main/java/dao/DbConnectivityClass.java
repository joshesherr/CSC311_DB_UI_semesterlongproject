package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import model.Major;
import model.Person;
import service.MyLogger;

import java.sql.*;
import java.util.List;

public class     DbConnectivityClass {
    final static String DB_NAME="CSC311_BD_TEMP";
    MyLogger lg= new MyLogger();
    final static String SQL_SERVER_URL = "";
    final static String DB_URL = SQL_SERVER_URL+DB_NAME;
    final static String USERNAME = "";
    final static String PASSWORD = "";

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.
    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data");
            }
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, first_name, last_name, department, Major.valueOf(major), email, imageURL));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }


    public boolean connectToDatabase() {
        boolean hasRegistredUsers = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            //First, connect to MYSQL server and create the database if not created
            Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS "+DB_NAME+"");
            statement.close();
            conn.close();

            //Second, connect to the database and create the table "users" if cot created
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (" + "id INT( 10 ) NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "first_name VARCHAR(200) NOT NULL," + "last_name VARCHAR(200) NOT NULL,"
                    + "department VARCHAR(200),"
                    + "major VARCHAR(200),"
                    + "email VARCHAR(200) NOT NULL UNIQUE,"
                    + "imageURL VARCHAR(200))";
            statement.executeUpdate(sql);

            //check if we have users in the table users
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegistredUsers = true;
                }
            }

            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegistredUsers;
    }

    public void queryUserByLastName(String name) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE last_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String major = resultSet.getString("major");
                String department = resultSet.getString("department");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Major: " + major + ", Department: " + department);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listAllUsers() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Department: " + department + ", Major: " + major + ", Email: " + email);
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Method to insert one record into the database
    public Task<Boolean> insertUser(Person person) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                boolean result = false;
                try {
                    updateMessage("Inserting Student...");
                    connectToDatabase();
                    Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                    String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, person.getFirstName());
                    preparedStatement.setString(2, person.getLastName());
                    preparedStatement.setString(3, person.getDepartment());
                    preparedStatement.setString(4, person.getMajor().name());
                    preparedStatement.setString(5, person.getEmail());
                    preparedStatement.setString(6, person.getImageURL());
                    int row = preparedStatement.executeUpdate();
                    if (row > 0) {
                        lg.makeLog("A new student was inserted successfully.");
                        result=true;
                        updateMessage("Insert Successful!");
                    }
                    else {
                        updateMessage("Insert Failed!");
                        cancel();
                    }
                    preparedStatement.close();
                    conn.close();
                } catch (SQLException e) {
                    updateMessage("Insert Failed! " + e.getMessage());
                    cancel();
                }
                return result;
            }
        };
    }

    //Method to insert many records in the database at once.
    public Task<Boolean> insertAllUser(List<Person> persons) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                boolean result = false;
                connectToDatabase();
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                for (int i = 0; i < persons.size(); i++)  {
                    updateProgress(i+1, persons.size());
                    try {
                        updateMessage("Inserting Students... "+(i+1)+"\\"+persons.size());
                        String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = conn.prepareStatement(sql);
                        preparedStatement.setString(1, persons.get(i).getFirstName());
                        preparedStatement.setString(2, persons.get(i).getLastName());
                        preparedStatement.setString(3, persons.get(i).getDepartment());
                        preparedStatement.setString(4, persons.get(i).getMajor().name());
                        preparedStatement.setString(5, persons.get(i).getEmail());
                        preparedStatement.setString(6, persons.get(i).getImageURL());
                        int row = preparedStatement.executeUpdate();
                        if (row > 0) {
                            lg.makeLog("A new student was inserted successfully.");
                            result=true;
                        }
                        preparedStatement.close();
                    } catch (SQLException e) {
                        updateMessage("Insertion Failed! " + e.getMessage());
                        cancel();
                    }
                }
                conn.close();
                updateProgress(1, 1);
                updateMessage("Insertion Successful!");
                return result;
            }
        };
    }

    //Method to edit one record in the database
    public Task<Boolean> editUser(int id, Person p) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    updateMessage("Editing Student...");
                    connectToDatabase();
                    Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                    String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, p.getFirstName());
                    preparedStatement.setString(2, p.getLastName());
                    preparedStatement.setString(3, p.getDepartment());
                    preparedStatement.setString(4, p.getMajor().name());
                    preparedStatement.setString(5, p.getEmail());
                    preparedStatement.setString(6, p.getImageURL());
                    preparedStatement.setInt(7, id);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    conn.close();
                    updateMessage("Edit Successful!");
                    return true;
                } catch (SQLException e) {
                    updateMessage("Edit Failed! " + e.getMessage());
                    cancel();
                    return false;
                }
            }
        };
    }

    //Method to delete one record in the database
    public Task<Boolean> deleteRecord(Person person) {
        int id = person.getId();
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws RuntimeException {
                try {
                    updateMessage("Deleting Student...");
                    connectToDatabase();
                    Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                    String sql = "DELETE FROM users WHERE id=?";
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setInt(1, id);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    conn.close();
                    updateMessage("Delete Successful!");
                    return true;
                } catch (SQLException e) {
                    updateMessage("Delete Failed! " + e.getMessage());
                    cancel();
                    return false;
                }
            }
        };
    }

    //method to delete all records in the database
    public Task<Void> deleteAllRecords() {
        return new Task<Void>() {
            @Override
            protected Void call() throws RuntimeException {
                try {
                    updateMessage("Deleting All Students...");
                    connectToDatabase();
                    Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                    String sql =
                            "CREATE TABLE users ("
                                    + "id INT( 10 ) NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                                    + "first_name VARCHAR(200) NOT NULL," + "last_name VARCHAR(200) NOT NULL,"
                                    + "department VARCHAR(200),"
                                    + "major VARCHAR(200),"
                                    + "email VARCHAR(200) NOT NULL UNIQUE,"
                                    + "imageURL VARCHAR(200))";
                    Statement statement = conn.createStatement();
                    statement.execute("DROP TABLE IF EXISTS users");
                    statement.execute(sql);
                    conn.close();
                    updateMessage("Deletion Successful!");
                } catch (SQLException e) {
                    updateMessage("Deletion Failed! " + e.getMessage());
                    cancel();
                }
                return null;
            }
        };
    }

    //Method to retrieve id from database where it is auto-incremented.
    public int retrieveId(Person p) {
        connectToDatabase();
        int id;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            id = resultSet.getInt("id");
            preparedStatement.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        lg.makeLog(String.valueOf(id));
        return id;
    }
}
