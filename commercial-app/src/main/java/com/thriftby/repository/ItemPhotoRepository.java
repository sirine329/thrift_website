package com.thriftby.repository;

import com.thriftby.entity.ItemPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPhotoRepository extends JpaRepository<ItemPhoto, Long> {
}
