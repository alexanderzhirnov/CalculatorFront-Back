package backend.back.repository;

import backend.back.entity.Calculation;
import backend.back.entity.User;
import backend.back.entity.enums.CalculationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {
    List<Calculation> findAllByClientId(Long clientId);
    List<Calculation> findAllByClientIdAndClientCreatedByOrderByCreatedAtDesc(Long clientId, User user);
    Optional<Calculation> findByIdAndClientCreatedBy(Long id, User user);
    List<Calculation> findAllByStatusAndPricesFixedUntilBefore(
            CalculationStatus status, LocalDateTime dateTime
    );  // для задачи по истечению 10 дней
}
