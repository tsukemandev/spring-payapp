package net.paysm.applymailservice.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import net.paysm.applymailservice.entity.Merchant;

public interface MerchantRepository extends CrudRepository<Merchant, Long> {

    Optional<Merchant> findByIdAndPassword(String id, String password);

    Boolean existsById (String id);

    Optional<Merchant> findByPassword (String password);

    Optional<Merchant> findByMid (String mid);
} 
