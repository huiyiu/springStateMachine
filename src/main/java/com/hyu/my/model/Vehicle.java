package com.hyu.my.model;

import com.hyu.my.state.States;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "vehicle")
@Getter
@Setter
public class Vehicle {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Column
    private String type;
    @Column
    private String businessType;
    @Column
    @Enumerated(EnumType.STRING)
    private States state;
    @Column
    private String logistics;
    @Column
    private String riskType;
    @Column
    private String conclusion;
}
