package com.usetech.dvente.entities.products;

import com.usetech.dvente.entities.BaseModel;
import com.usetech.dvente.entities.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class DeliveryAddress extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String phone;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String textMessage;

    private String audioMessage;

    @Override
    public String toString() {
        return "Adresse de livraison de " + email;
    }
}
