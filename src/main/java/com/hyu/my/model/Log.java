package com.hyu.my.model;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "log")
@Getter
@Setter
public class Log {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    @Column
    private String currentStatus;
    @Column
    private String action;
    @Column
    private String conclusion;
}
