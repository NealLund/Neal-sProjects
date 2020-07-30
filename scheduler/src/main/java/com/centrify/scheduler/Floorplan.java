package com.centrify.scheduler;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

//Creation of floorplan object to be used in database
@Repository
public interface Floorplan extends CrudRepository<Cubicle, String> {
	
}
