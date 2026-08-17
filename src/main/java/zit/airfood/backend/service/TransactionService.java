package zit.airfood.backend.service;


import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.entity.ServicePointEntity;
import zit.airfood.backend.dao.entity.TicketEntity;
import zit.airfood.backend.dao.entity.TransactionEntity;
import zit.airfood.backend.dao.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final ServicePointService servicePointService;

    @Transactional
    public TransactionEntity createTopUpTransaction(TicketEntity ticket, BigDecimal amount, int servicePointId) {
        ServicePointEntity servicePoint = servicePointService.findEntityById(servicePointId);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setTicket(ticket);
        transaction.setAmount(amount);
        transaction.setServicePoint(servicePoint);
        transaction.setType(TransactionEntity.Type.topUp);
        transaction.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionEntity createPurchaseTransaction(TicketEntity ticket, BigDecimal amount, int servicePointId) {
        ServicePointEntity servicePoint = servicePointService.findEntityById(servicePointId);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setTicket(ticket);
        transaction.setAmount(amount);
        transaction.setServicePoint(servicePoint);
        transaction.setType(TransactionEntity.Type.purchase);
        transaction.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
}
