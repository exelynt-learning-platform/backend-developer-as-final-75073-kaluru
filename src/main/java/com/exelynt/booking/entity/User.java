package com.exelynt.booking.entity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="users", uniqueConstraints={@UniqueConstraint(columnNames="username"),@UniqueConstraint(columnNames="email")}) public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,length=50) private String username; @Column(nullable=false,length=255) private String email; @Column(nullable=false) private String password; @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private Role role; @Column(nullable=false,updatable=false) private Instant createdAt; @Column(nullable=false) private Instant updatedAt;
 @PrePersist void create(){createdAt=updatedAt=Instant.now();} @PreUpdate void update(){updatedAt=Instant.now();} public Long getId(){return id;} public String getUsername(){return username;} public String getEmail(){return email;} public String getPassword(){return password;} public Role getRole(){return role;} public void setUsername(String v){username=v;} public void setEmail(String v){email=v;} public void setPassword(String v){password=v;} public void setRole(Role v){role=v;}
}
