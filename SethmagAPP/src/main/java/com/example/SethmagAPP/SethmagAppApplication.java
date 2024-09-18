package com.example.SethmagAPP;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
// Main Application Class
@SpringBootApplication
public class SethmagAppApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(SethmagAppApplication.class, args);
	}
	@Override
	public void run(String... args) {
		System.out.println("Application Started!");
	}
}
// AccountService Interface
interface AccountService {
	void withdraw(String accountNum, BigDecimal amountToWithdraw);
	void deposit(String accountNum, BigDecimal amountToDeposit); // New method for deposits
	Account getAccount(String accountNum); // Method for getting account details
}
// Abstract Account Class
abstract class Account {
	protected String accountNum;
	protected BigDecimal balance;
	public Account(String accountNum, BigDecimal balance) {
		this.accountNum = accountNum;
		this.balance = balance;
	}
	public abstract void withdraw(BigDecimal amountToWithdraw) throws Exception;
	public String getAccountNum() {
		return accountNum;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	protected void setBalance(BigDecimal newBalance) {
		this.balance = newBalance;
	}
	public void deposit(BigDecimal amountToDeposit) {
		setBalance(balance.add(amountToDeposit)); // Adds the deposit to the balance
	}
}
// SavingsAccount Class
class SavingsAccount extends Account {
	private static final BigDecimal MINIMUM_BALANCE = BigDecimal.valueOf(1000);
	public SavingsAccount(String accountNum, BigDecimal balance) {
		super(accountNum, balance);
	}
	@Override
	public void withdraw(BigDecimal amountToWithdraw) throws Exception {
		if (balance.subtract(amountToWithdraw).compareTo(MINIMUM_BALANCE) < 0) {
			throw new Exception("Insufficient funds. Savings account must maintain a minimum balance of R1000.");
		}
		setBalance(balance.subtract(amountToWithdraw));
	}
}
// CurrentAccount Class
class CurrentAccount extends Account {
	private static final BigDecimal MAXIMUM_OVERDRAFT = BigDecimal.valueOf(100000);
	public CurrentAccount(String accountNum, BigDecimal balance) {
		super(accountNum, balance);
	}
	@Override
	public void withdraw(BigDecimal amountToWithdraw) throws Exception {
		BigDecimal overdraftLimit = balance.add(MAXIMUM_OVERDRAFT);
		if (balance.subtract(amountToWithdraw).compareTo(overdraftLimit.negate()) < 0) {
			throw new Exception("Insufficient funds. Maximum overdraft limit is R100,000.");
		}
		setBalance(balance.subtract(amountToWithdraw));
	}
}
// AccountServiceImpl Class (Service Layer)
@Service
class AccountServiceImpl implements AccountService {
	private final Map<String, Account> accounts = new HashMap<>();
	public AccountServiceImpl() {
		// Initializing some test accounts
		accounts.put("SAV123", new SavingsAccount("SAV123", BigDecimal.valueOf(5000)));
		accounts.put("CUR456", new CurrentAccount("CUR456", BigDecimal.valueOf(10000)));
	}
	@Override
	public void withdraw(String accountNum, BigDecimal amountToWithdraw) {
		Account account = accounts.get(accountNum);
		if (account == null) {
			throw new IllegalArgumentException("Account not found");
		}
		try {
			account.withdraw(amountToWithdraw);
			System.out.println("Withdrawal successful. New balance: " + account.getBalance());
		} catch (Exception e) {
			System.out.println("Error during withdrawal: " + e.getMessage());
		}
	}
	@Override
	public void deposit(String accountNum, BigDecimal amountToDeposit) {
		Account account = accounts.get(accountNum);
		if (account == null) {
			throw new IllegalArgumentException("Account not found");
		}
		account.deposit(amountToDeposit);
		System.out.println("Deposit successful. New balance: " + account.getBalance());
	}
	@Override
	public Account getAccount(String accountNum) {
		return accounts.get(accountNum); // Returns the account details for balance check
	}
}
// BankController Class (REST API Controller)
@RestController
@RequestMapping("/api/bank")
class BankController {
	private final AccountService accountService;
	@Autowired
	public BankController(AccountService accountService) {
		this.accountService = accountService;
	}
	// GET request for withdrawal
	@GetMapping("/withdraw")
	public String withdrawGet(@RequestParam String accountNum, @RequestParam BigDecimal amount) {
		try {
			accountService.withdraw(accountNum, amount);
			return "Withdrawal successful";
		} catch (IllegalArgumentException e) {
			return "Account not found.";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	// POST request for withdrawal
	@PostMapping("/withdraw")
	public String withdrawPost(@RequestParam String accountNum, @RequestParam BigDecimal amount) {
		try {
			accountService.withdraw(accountNum, amount);
			return "Withdrawal successful";
		} catch (IllegalArgumentException e) {
			return "Account not found.";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	// NEW: GET request for checking balance
	@GetMapping("/balance")
	public String checkBalance(@RequestParam String accountNum) {
		try {
			Account account = accountService.getAccount(accountNum);
			if (account == null) {
				return "Account not found.";
			}
			return "Balance for account " + accountNum + ": " + account.getBalance();
		} catch (IllegalArgumentException e) {
			return "Account not found.";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	// NEW: POST request for deposit
	@PostMapping("/deposit")
	public String depositPost(@RequestParam String accountNum, @RequestParam BigDecimal amount) {
		try {
			accountService.deposit(accountNum, amount);
			return "Deposit successful";
		} catch (IllegalArgumentException e) {
			return "Account not found.";
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
}