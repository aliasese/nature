package com.cnebula.nature.dto;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "nature.dbo.author_aff")
public class AuthAff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "au_affid", nullable = false)
    private Integer auAffid;

    @Column(name = "aid", nullable = false)
    private Integer aid;

    @Column(name = "affid", nullable = false)
    private Integer affid;
}
