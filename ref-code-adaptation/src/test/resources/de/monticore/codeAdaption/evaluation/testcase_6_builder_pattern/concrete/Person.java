package de.monticore.codeAdaption.evaluation.testcase_6_builder_pattern;

import java.util.List;

/**
 * Person domain class with attributes
 */
public class Person {
    private String name;
    private int age;
    private String email;
    private String phone;
    private List<String> tags;

    public Person() {
    }

    public Person(String name, int age, String email, String phone, List<String> tags) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getTags() {
        return tags;
    }

    public void sX(Object x) {}

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", tags=" + tags +
                '}';
    }
}

