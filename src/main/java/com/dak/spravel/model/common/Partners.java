package com.dak.spravel.model.common;

import com.dak.spravel.model.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "partners")
public class Partners extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    public enum Plan {
        BASIC, PRO, ENTERPRISE;

        @JsonCreator
        public static Plan fromObject(Map<String, Object> obj) {
            if (obj != null && obj.containsKey("name")) {
                return Plan.valueOf(obj.get("name").toString().toUpperCase());
            }
            return null;
        }
    }

    @Enumerated(EnumType.STRING)
    private Plan plan;

    @Column(name = "is_active")
    private Boolean isActive = true;

}
