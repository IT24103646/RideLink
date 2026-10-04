package com.ridelink.driver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drivers")
public class Driver {
    @Id
    private String id;

    @Indexed(unique = true)
    private String accountId;

    private String name;
    private String phone;
    private Vehicle vehicle;
    private Availability availability;
    private Location location;
    private ServiceArea serviceArea;

    protected Driver() {
    }

    public Driver(String accountId, String name, String phone, Vehicle vehicle, Availability availability,
                  Location location, ServiceArea serviceArea) {
        this.accountId = accountId;
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.availability = availability;
        this.location = location;
        this.serviceArea = serviceArea;
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Vehicle getVehicle() { return vehicle; }
    public Availability getAvailability() { return availability; }
    public Location getLocation() { return location; }
    public ServiceArea getServiceArea() { return serviceArea; }
    public void setId(String id) { this.id = id; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public void setAvailability(Availability availability) { this.availability = availability; }
    public void setLocation(Location location) { this.location = location; }
    public void setServiceArea(ServiceArea serviceArea) { this.serviceArea = serviceArea; }
}
