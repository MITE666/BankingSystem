import model.BankAccount;
import repository.*;
import service.*;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {
        BankRepository bankRepository = new JDBCBankRepository();
        BankService bankService = new BankService(bankRepository);
        LoanRepository loanRepository = new JDBCLoanRepository();
        LoanService loanService = new LoanService(loanRepository);
        while (true) {
            displayMenu(bankService, loanService);
        }
    }

    private static void displayMenu(BankService bankService, LoanService loanService) throws SQLException {
        System.out.println("""
                
                Select an option from the menu below:
                1. Add account (checking, CoD, MM, savings)
                2. Deposit to account
                3. Withdraw from account
                4. Create transfer
                5. Display customer information
                6. Display transactions
                7. Increment saving accounts
                8. Add loan
                9. Get monthly rate
                10. Display loans
                11. Make payment
                12. Exit
                
                """);

        Scanner scanner = new Scanner(System.in);
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1 -> addAccount(bankService);
            case 2 -> addDeposit(bankService);
            case 3 -> addWithdrawal(bankService);
            case 4 -> addTransfer(bankService);
            case 5 -> displayCustomer(bankService);
            case 6 -> displayTransactions(bankService);
            case 7 -> incrementSavings(bankService);
            case 8 -> addLoan(loanService);
            case 9 -> displayMonthlyRate(loanService);
            case 10 -> displayLoans(loanService);
            case 11 -> makePayment(loanService);
            case 12 -> System.exit(0);
            default -> System.out.println("Invalid option");
        }
    }

    private static void addAccount(BankService bankService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Customer name:");
        String name = scanner.nextLine();
        System.out.println("Initial balance:");
        double balance = scanner.nextDouble();
        System.out.println("Account type:");
        String type = scanner.next();
        bankService.createAccount(name, balance, type);
    }

    private static void addDeposit(BankService bankService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Account number:");
        int number = scanner.nextInt();
        System.out.println("Amount:");
        double amount = scanner.nextDouble();
        bankService.deposit(number, amount);
    }

    private static void addWithdrawal(BankService bankService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Account number:");
        int number = scanner.nextInt();
        System.out.println("Amount:");
        double amount = scanner.nextDouble();
        bankService.withdraw(number, amount);
    }

    private static void addTransfer(BankService bankService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("From:");
        int src = scanner.nextInt();
        System.out.println("To:");
        int dst = scanner.nextInt();
        System.out.println("Amount:");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.println("Description:");
        String description = scanner.nextLine();
        bankService.transfer(src, dst, amount, description);
    }

    private static void displayCustomer(BankService bankService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Customer name:");
        String name = scanner.nextLine();
        bankService.displayCustomer(name);
    }

    private static void displayTransactions(BankService bankService) throws SQLException {
        bankService.displayTransactions();
    }

    private static void incrementSavings(BankService bankService) throws SQLException {
        bankService.incrementSavings();
    }

    private static void addLoan(LoanService loanService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Customer name:");
        String name = scanner.nextLine();
        System.out.println("Loan amount:");
        double amount = scanner.nextDouble();
        System.out.println("Repayment period:");
        int period = scanner.nextInt();
        loanService.addLoan(name, amount, period);
    }

    private static void displayMonthlyRate(LoanService loanService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Loan ID:");
        int id = scanner.nextInt();
        loanService.displayMonthlyPayment(id);
    }

    private static void displayLoans(LoanService loanService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Customer name:");
        String name = scanner.nextLine();
        loanService.displayLoans(name);
    }

    private static void makePayment(LoanService loanService) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Loan ID:");
        int id = scanner.nextInt();
        System.out.println("Payment amount:");
        double amount = scanner.nextDouble();
        loanService.makePayment(id, amount);
    }
}