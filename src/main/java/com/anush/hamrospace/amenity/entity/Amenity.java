package com.anush.hamrospace.amenity.entity;

import com.anush.hamrospace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "amenities")
public class Amenity extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String iconUrl;

    @Column(name = "is_featured", nullable = false)
    private boolean featured = false; 

    @Column(name = "is_active", nullable = false)
    private boolean active=true;

}
