package service.carsharing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(nullable = false)
    private Integer inventory;
    @Column(nullable = false)
    private BigDecimal fee;
    @Column(nullable = false)
    private boolean deleted = false;

    public enum Type {
        SEDAN,
        SUV,
        CUV,
        COUPE,
        HATCHBACK,
        MINIVAN,
        UNIVERSAL,
        MICRO
    }

    @PreRemove
    public void preRemove() {
        this.deleted = true;
    }
}
