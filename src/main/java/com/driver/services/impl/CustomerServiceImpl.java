package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();

		List<Integer> driverId = new ArrayList<>();
		for(Driver driver : drivers) {
			if (driver.getCab().getAvailable() == true) {
				int id = driver.getDriverId();
				driverId.add(id);
			}
		}
		if(driverId.size() == 0){
			throw new Exception("No cab available!");
		}
		else {
			Collections.sort(driverId);
			TripBooking tripBooking = new TripBooking();
			Customer customer = customerRepository2.findById(customerId).get();
			int bookingId = driverId.get(0);
			Driver driver = driverRepository2.findById(bookingId).get();

			tripBooking.setStatus(TripStatus.CONFIRMED);
			tripBooking.setFromLocation(fromLocation);
			tripBooking.setToLocation(toLocation);
			tripBooking.setDistanceInKm(distanceInKm);
			tripBooking.setCustomer(customer);
			tripBooking.setDriver(driver);

			List<TripBooking> list = customer.getTripBookingList();
			list.add(tripBooking);
			customer.setTripBookingList(list);

			List<TripBooking> list1 = driver.getTripBooking();
			list1.add(tripBooking);
			driver.setTripBooking(list1);

			driverRepository2.save(driver);
			customerRepository2.save(customer);

			return tripBooking;
		}

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		Driver driver = tripBooking.getDriver();
		tripBooking.setStatus(TripStatus.CANCELED);
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		Driver driver = tripBooking.getDriver();
		tripBooking.setStatus(TripStatus.COMPLETED);
		driver.getCab().setAvailable(true);

		driverRepository2.save(driver);

	}
}
