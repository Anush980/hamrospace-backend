package com.anush.hamrospace.user.repository;

import java.util.Optional;

import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;

public interface ProfileRepository extends AbstractRepository<Profile>{
    Optional<Profile> findByUserAndDeletedAtIsNull(User user);
}
