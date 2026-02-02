package de.caransgar.chorehub.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String shortname;

    @OneToMany(mappedBy = "assignedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chore> chores;

    // Constructors
    public User() {}

    public User(String name, String shortname) {
        this.name = name;
        this.shortname = shortname;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public List<Chore> getChores() {
        return chores;
    }

    public void setChores(List<Chore> chores) {
        this.chores = chores;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shortname='" + shortname + '\'' +
                '}';
    }
}