package com.bank.api.repository;

import com.bank.api.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByAccountNumberOrderByTimestampDesc(String accountNumber);
}