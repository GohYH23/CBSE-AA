package Customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String category; // e.g., "Retail", "Wholesale"

    // Default Constructor (Required by JPA)
    public Customer() {}

    // Constructor for easy creation
    public Customer(String name, String email, String category) {
        this.name = name;
        this.email = email;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', email='" + email + "', category='" + category + "'}";
    }
}