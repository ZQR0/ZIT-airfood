package zit.airfood.backend.dao.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.ServicePointEntity;
import zit.airfood.backend.dao.entity.TicketEntity;
import zit.airfood.backend.dao.entity.TransactionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void count_returnsTwentyEightSeededTransactions() {
        assertThat(transactionRepository.count()).isEqualTo(28L);
    }

    @Test
    void findByTicketId_returnsAllForTicket() {
        List<TransactionEntity> transactions = transactionRepository.findByTicketId(4);
        assertThat(transactions).hasSize(3);
    }

    @Test
    void findByTicketId_returnsEmptyForTicketWithoutTransactions() {
        assertThat(transactionRepository.findByTicketId(2)).isEmpty();
    }

    @Test
    void findByServicePointId_returnsTransactionsAtServicePoint() {
        List<TransactionEntity> transactions = transactionRepository.findByServicePointId(1);
        assertThat(transactions).hasSize(6);
    }

    @Test
    void save_persistsNewTransaction() {
        TicketEntity ticket = entityManager.getReference(TicketEntity.class, 1);
        ServicePointEntity sp = entityManager.getReference(ServicePointEntity.class, 1);
        TransactionEntity tx = new TransactionEntity(
                new BigDecimal("50.00"),
                LocalDateTime.of(2026, 7, 18, 12, 0),
                sp,
                ticket,
                TransactionEntity.Type.purchase
        );

        TransactionEntity saved = transactionRepository.save(tx);
        transactionRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(transactionRepository.findByTicketId(1)).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findById_eagerlyLoadsTicketAndServicePoint() {
        TransactionEntity tx = transactionRepository.findById(1).orElseThrow();
        assertThat(tx.getTicket()).isNotNull();
        assertThat(tx.getTicket().getId()).isEqualTo(4);
        assertThat(tx.getServicePoint()).isNotNull();
        assertThat(tx.getServicePoint().getId()).isEqualTo(1);
    }
}
