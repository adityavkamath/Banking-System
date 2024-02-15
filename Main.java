package com.example.demo;

import java.sql.*;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/bank";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            BankEmployee1 bankEmployee = new BankEmployee1();
            Transaction1 transaction = new Transaction1();

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Bank Employee Operations");
                System.out.println("2. User Transaction Operations");
                System.out.println("3. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        bankEmployee.run();
                        break;
                    case 2:
                        transaction.run();
                        break;
                    case 3:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            }

            System.out.println("Exiting...");
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class BankEmployee1 {
    private Scanner scanner;

    public BankEmployee1() {
        scanner = new Scanner(System.in);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(Main.JDBC_URL, Main.USERNAME, Main.PASSWORD);
    }

    public void run() {
        try {
            boolean exit = false;
            while (!exit) {
                System.out.println("\nBank Employee Operations:");
                System.out.println("1. Open New Account");
                System.out.println("2. Open Existing Data");
                System.out.println("3. Register ATM Agency");
                System.out.println("4. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        openNewAccount();
                        break;
                    case 2:
                        openExistingData();
                        break;
                    case 3:
                        registerAtmAgency();
                        break;
                    case 4:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openNewAccount() {
        try (Connection connection = connect()) {
            System.out.println("Enter customer name:");
            String name = scanner.nextLine();

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO customer_accounts (name, balance) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);
                preparedStatement.setDouble(2, 0.0);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int accountNumber = generatedKeys.getInt(1);
                            System.out.println("Account opened successfully for " + name + ". Account Number: " + accountNumber);
                        }
                    }
                } else {
                    System.out.println("Failed to open account for " + name);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error opening new account: " + e.getMessage());
        }
    }

    public void openExistingData() {
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM customer_accounts")) {

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No existing data.");
                return;
            }

            System.out.println("Existing data:");
            while (resultSet.next()) {
                int accountNumber = resultSet.getInt("account_number");
                String name = resultSet.getString("name");
                double balance = resultSet.getDouble("balance");

                System.out.println("Account Number: " + accountNumber + ", Name: " + name + ", Balance: " + balance);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving existing data: " + e.getMessage());
        }
    }

    public void registerAtmAgency() {
        System.out.println("Registering at ATM agency...");
        // Add your logic here if needed
    }
}

class Transaction1 {
    private Scanner scanner;

    public Transaction1() {
        scanner = new Scanner(System.in);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(Main.JDBC_URL, Main.USERNAME, Main.PASSWORD);
    }

    public void run() {
        try {
            boolean exit = false;
            while (!exit) {
                System.out.println("\nTransaction Operations:");
                System.out.println("1. Do Transaction");
                System.out.println("2. Withdraw Money");
                System.out.println("3. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        doTransaction();
                        break;
                    case 2:
                        withdrawMoney();
                        break;
                    case 3:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doTransaction() {
        try (Connection connection = connect()) {
            System.out.println("Enter account number for the transaction:");
            int accountNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            CustomerAccount account = findAccount(connection, accountNumber);

            if (account != null) {
                System.out.println("Enter amount to deposit:");
                double amount = scanner.nextDouble();
                scanner.nextLine(); // Consume newline

                if (amount > 0) {
                    account.deposit(amount, connection);
                    System.out.println("Transaction successful. Current balance for Account " + accountNumber + ": " + account.getBalance());
                } else {
                    System.out.println("Invalid amount. Transaction failed.");
                }
            } else {
                System.out.println("Account not found for Account Number " + accountNumber);
            }
        } catch (SQLException e) {
            System.out.println("Error processing transaction: " + e.getMessage());
        }
    }

    public void withdrawMoney() {
        try (Connection connection = connect()) {
            System.out.println("Enter account number for the withdrawal:");
            int accountNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            CustomerAccount account = findAccount(connection, accountNumber);

            if (account != null) {
                System.out.println("Enter amount to withdraw:");
                double amount = scanner.nextDouble();
                scanner.nextLine(); // Consume newline

                if (amount > 0 && amount <= account.getBalance()) {
                    account.withdraw(amount, connection);
                    System.out.println("Withdrawal successful. Current balance for Account " + accountNumber + ": " + account.getBalance());
                } else {
                    System.out.println("Invalid amount or insufficient funds. Withdrawal failed.");
                }
            } else {
                System.out.println("Account not found for Account Number " + accountNumber);
            }
        } catch (SQLException e) {
            System.out.println("Error processing withdrawal: " + e.getMessage());
        }
    }

    private CustomerAccount findAccount(Connection connection, int accountNumber) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM customer_accounts WHERE account_number = ?")) {
            preparedStatement.setInt(1, accountNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    double balance = resultSet.getDouble("balance");
                    return new CustomerAccount(accountNumber, name, balance);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding account: " + e.getMessage());
            throw e;
        }

        return null;
    }
}

class CustomerAccount {
    private int accountNumber;
    private String name;
    private double balance;

    public CustomerAccount(int accountNumber, String name, double balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount, Connection connection) throws SQLException {
        balance += amount;
        updateBalanceInDatabase(connection);
    }

    public void withdraw(double amount, Connection connection) throws SQLException {
        balance -= amount;
        updateBalanceInDatabase(connection);
    }

    private void updateBalanceInDatabase(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE customer_accounts SET balance = ? WHERE account_number = ?")) {
            preparedStatement.setDouble(1, balance);
            preparedStatement.setInt(2, accountNumber);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public String toString() {
        return "Account Number: " + accountNumber + ", Name: " + name + ", Balance: " + balance;
    }
}