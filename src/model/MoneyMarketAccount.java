package model;

public class MoneyMarketAccount extends BankAccount {
    private double interestRate;
    private double withdrawalLimit;

    public MoneyMarketAccount(int accountNumber, double interestRate, double withdrawalLimit) {
        super(accountNumber);
        this.interestRate = interestRate;
        this.withdrawalLimit = withdrawalLimit;
    }

    @Override
    public boolean withdraw(double amount) {
        if (getBalance() >= amount && withdrawalLimit > 0) {
            balance -= amount;
            withdrawalLimit--;
            return true;
        }
        return false;
    }

    public void calculateInterest() {
        double interest = getBalance() * interestRate / 100;
        deposit(interest);
    }

    @Override
    public String toString() {
        return "Money Market Account" + super.toString();
    }
}
