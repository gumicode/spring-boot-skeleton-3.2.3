package com.gumicode.database.member.entity;

import com.gumicode.database.member.converter.PasswordConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Builder
@Entity
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {@Index(columnList = "username")})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Convert(converter = PasswordConverter.class)
    private String password;
}
