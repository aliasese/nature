package com.cnebula.nature.dto;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "nature.dbo.content")
public class Affiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "affid", nullable = false)
    private Integer affid;

    @Column(name = "artid", nullable = false)
    //@OneToMany(targetEntity = Article.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Integer artid;

    @Column(name = "aff", nullable = false)
    private String aff;

    @Column(name = "affsort", nullable = true)
    private Integer affsort;

}
