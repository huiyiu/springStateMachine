package com.hyu.my.repo;

import com.hyu.my.model.Log;
import org.springframework.data.repository.CrudRepository;

public interface LogRepo extends CrudRepository<Log,String> {
}
