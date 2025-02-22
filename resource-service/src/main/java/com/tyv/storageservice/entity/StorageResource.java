package com.tyv.storageservice.entity;

import com.tyv.storageservice.enums.Bucket;
import com.tyv.storageservice.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "storage_resource")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class StorageResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @CreatedDate
    @Column(name = "created_at")
    LocalDateTime createAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updateAt;

    @Column(name = "uuid")
    UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "bucket")
    Bucket bucket;

    @Column(name = "content_type")
    String contentType;

    @Column(name = "path")
    String path;

    @Column(name = "title")
    String title;

    @Transient
    byte[] data;
}
