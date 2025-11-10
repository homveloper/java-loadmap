package main.java.com.example.mongodb;

import org.springframework.data.annotation.Id;

public record Person(@Id String id, String firstName, String lastName) {

}
