package com.bankingapp.backend.model;



import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {
    @Column(name = "FirstName")
    private String firstName;
    @Column(name = "LastName")
    private String lastName;
    @Column(name = "Email")
    private String email;
    @Column(name = "Phone")
    private int phoneNumber;
    @Column(name = "countryId")
    private int countryId;
    @Column(name = "AddrLine1")
    private String addrLine1;
    @Column(name = "AddrLine2")
    private String addrLine2;
    @Column(name = "TownCity")
    private String townCity;
    @Column(name = "CountyState")
    private String countyState;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private int customerId;
    @Column(name = "Username")
    private String username;
    @Column(name = "Password")
    private String password;
    /* todo: dob, idtype and idnumber missing in the db */
    @Column(name = "dob")
    private String dob;
    @Column(name = "idType")
    private String idType;
    @Column(name = "idNumber")
    private String idNumber;
    @Column(name = "Role")
    private String role;


    @OneToMany (fetch = FetchType.EAGER)
    @JoinColumn(name = "CustomerId", referencedColumnName = "CustomerId")
    private List<Account> accounts;

    // Constructors
    public Customer() {}

    public Customer(String firstName, String lastName, String email, int phone, int countryId, String addrLine1, String addrLine2, String townCity, String countryState, String password, String idType, String idNumber, String dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phone;
        this.countryId = countryId;
        this.addrLine1 = addrLine1;
        this.addrLine2 = addrLine2;
        this.townCity = townCity;
        this.countyState = countryState;
        this.password = password;
        this.idType = idType;
        this.idNumber = idNumber;
        this.dob = dob;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phone) {
        this.phoneNumber = phone;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getAddrLine1() {
        return addrLine1;
    }

    public void setAddrLine1(String homeAddress) {
        this.addrLine1 = homeAddress;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public String getTownCity() {
        return townCity;
    }

    public String getCountyState() {
        return countyState;
    }

    public void setAddrLine2(String addrLine2) {
        this.addrLine2 = addrLine2;
    }

    public void setTownCity(String townCity) {
        this.townCity = townCity;
    }

    public void setCountyState(String countryState) {
        this.countyState = countryState;
    }

    public String getDob() { return dob; }

    public String getUsername() { return username; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDob(String dob) { this.dob = dob; }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getRoles() { return role; }

    public void setRoles(String role) { this.role = role; }
}
