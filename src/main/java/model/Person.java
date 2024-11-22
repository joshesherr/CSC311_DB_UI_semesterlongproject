package model;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Person {
    private Integer id;
    private String firstName;
    private String lastName;
    private String department;
    private Major major;
    private String email;
    private String imageURL;

    public Person() {
    }

    public Person(String firstName, String lastName, String department, Major major, String email,  String imageURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.major = major;
        this.email = email;
        this.imageURL = imageURL;
    }

    public Person(Integer id, String firstName, String lastName, String department, Major major, String email,  String imageURL) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.major = major;
        this.email = email;
        this.imageURL = imageURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


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


    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", major='" + major + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    //convert person's fields into a csv friendly format.
    public String toCSVData() {
        String[] data = new String[] {id.toString(),firstName,lastName,department,major.name(),email,imageURL};
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }
    //check for special characters in data.
    private String escapeSpecialCharacters(String data) {
        if (data == null)  throw new IllegalArgumentException("Input data cannot be null");

        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}