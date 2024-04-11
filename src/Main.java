import model.BankAccount;
import service.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        BankService bankService = new BankService();
        while (true) {
            displayMenu(bankService);
        }
    }

    private static void displayMenu(BankService bankService) {
        System.out.println("""
                
                Select an option from the menu below:
                1. Add account
                2. Deposit to account
                3. Withdraw from account
                4. Create transfer
                5. Display customer information
                6. Exit
                
                """);

        Scanner scanner = new Scanner(System.in);
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1 -> addAccount(bankService);
            case 2 -> addDeposit(bankService);
            case 3 -> addWithdrawal(bankService);
            case 4 -> addTransfer(bankService);
            case 5 -> displayCustomer(bankService);
            case 6 -> System.exit(0);
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

    private static void addDeposit(BankService bankService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Account number:");
        int number = scanner.nextInt();
        System.out.println("Amount:");
        double amount = scanner.nextDouble();
        bankService.deposit(number, amount);
    }

    private static void addWithdrawal(BankService bankService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Account number:");
        int number = scanner.nextInt();
        System.out.println("Amount:");
        double amount = scanner.nextDouble();
        bankService.withdraw(number, amount);
    }

    private static void addTransfer(BankService bankService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("From:");
        int src = scanner.nextInt();
        System.out.println("To:");
        int dst = scanner.nextInt();
        System.out.println("Amount:");
        double amount = scanner.nextDouble();
        System.out.println("Description:");
        String description = scanner.next();
        bankService.transfer(src, dst, amount, description);
    }

    private static void displayCustomer(BankService bankService) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Customer name:");
        String name = scanner.next();
        bankService.displayCustomer(name);
    }
}