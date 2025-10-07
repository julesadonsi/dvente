package com.usetech.dvente.entities.users;

import com.usetech.dvente.entities.BaseModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
public class User extends BaseModel {

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UserRole role;

    @Builder.Default
    private boolean isStaff = false;

    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column
    private LocalDateTime dateJoined;

    @Column
    @Builder.Default
    private boolean emailConfirmed = false;

    @Column
    @Builder.Default
    private boolean phoneConfirmed = false;

    private String name;
    private String country;
    private String city;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private String password;

    public String getAvatarUrl() {
        if (avatar != null && !avatar.isEmpty()) {
            return avatar;
        }
        return String.format(
                "https://ui-avatars.com/api/?background=0D8ABC&color=fff&name=%s",
                name != null ? name : "User"
        );
    }

    @Override
    public String toString() {
        return (phone != null && !phone.isEmpty()) ? phone : email;
    }
}
