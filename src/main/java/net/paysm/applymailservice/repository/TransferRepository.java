package net.paysm.applymailservice.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import net.paysm.applymailservice.entity.Transfer;

public interface TransferRepository extends CrudRepository<Transfer, Long>{

    List<Transfer> findAllByMid(String mid);

    List<Transfer> findAllByMidOrMidOrderByAuthDateDesc(String mid1, String mid2, Pageable pageable);

    Transfer findByTid(String tid);
}
