package com.hyu.my.repo;

import com.hyu.my.model.Vehicle;
import org.springframework.data.repository.CrudRepository;

public interface VehicleRepo extends CrudRepository<Vehicle,Integer> {
}
