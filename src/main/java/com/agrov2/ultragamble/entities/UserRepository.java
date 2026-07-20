package com.agrov2.ultragamble.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<BotUser, Long> {
    List<BotUser> findTop10ByOrderByBalanceDesc();
}
