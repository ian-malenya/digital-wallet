package com.nexwallet.walletservice;

import com.nexwallet.walletservice.model.Wallet;
import com.nexwallet.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WalletService walletService;

    @Override
    public void run(String... args) throws Exception {

        // Only initialize if no wallets exist yet
        if (walletService.getAllWallets().isEmpty()) {

            // ── Create Wallets ──
            Wallet john = new Wallet();
            john.setOwnerName("John Doe");
            john.setEmail("john@example.com");
            john.setCurrency("KES");
            john = walletService.createWallet(john);

            Wallet jane = new Wallet();
            jane.setOwnerName("Jane Smith");
            jane.setEmail("jane@example.com");
            jane.setCurrency("KES");
            jane = walletService.createWallet(jane);

            Wallet acme = new Wallet();
            acme.setOwnerName("Acme Corp");
            acme.setEmail("acme@corp.com");
            acme.setCurrency("KES");
            acme = walletService.createWallet(acme);

            // ── John's Transactions ──
            walletService.deposit(john.getId(), new BigDecimal("50000"));
            walletService.deposit(john.getId(), new BigDecimal("30000"));
            walletService.withdraw(john.getId(), new BigDecimal("5000"));
            walletService.transfer(john.getId(), jane.getId(), new BigDecimal("10000"));
            walletService.transfer(john.getId(), acme.getId(), new BigDecimal("8000"));
            walletService.withdraw(john.getId(), new BigDecimal("3000"));

            // ── Jane's Transactions ──
            walletService.deposit(jane.getId(), new BigDecimal("20000"));
            walletService.withdraw(jane.getId(), new BigDecimal("2000"));
            walletService.transfer(jane.getId(), acme.getId(), new BigDecimal("5000"));

            // ── Acme's Transactions ──
            walletService.deposit(acme.getId(), new BigDecimal("100000"));
            walletService.withdraw(acme.getId(), new BigDecimal("15000"));
            walletService.transfer(acme.getId(), john.getId(), new BigDecimal("12000"));

            System.out.println("✅ Sample data initialized successfully!");
            System.out.println("👤 John Doe    → john@example.com");
            System.out.println("👤 Jane Smith  → jane@example.com");
            System.out.println("👤 Acme Corp   → acme@corp.com");
        } else {
            System.out.println("✅ Database already has data — skipping initialization.");
        }
    }
}