package com.forever.dadamda.entity.user;

import com.forever.dadamda.entity.BaseTimeEntity;
import com.forever.dadamda.entity.item.Item;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<Item> itemList = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String profileUrl;

    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String name, String email, String profileUrl, Provider provider, Role role) {
        this.name = name;
        this.email = email;
        this.profileUrl = profileUrl;
        this.provider = provider;
        this.role = role;
    }

    public User update(String name, String profileUrl) {
        this.name = name;
        this.profileUrl = profileUrl;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}