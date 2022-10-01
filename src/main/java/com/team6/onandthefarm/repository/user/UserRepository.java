package com.team6.onandthefarm.repository.user;

import com.team6.onandthefarm.entity.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Long> {

    Optional<User> findByUserEmailAndProvider(String email, String provider);
}
