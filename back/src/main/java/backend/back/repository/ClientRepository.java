package backend.back.repository;


import backend.back.entity.Client;
import backend.back.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findAllByCreatedBy(User user);           // клиенты менеджера
    List<Client> findAllByCreatedByOrderByCreatedAtDesc(User user);
    Optional<Client> findByIdAndCreatedBy(Long id, User user);
    Page<Client> findAll(Pageable pageable);               // с пагинацией
}
