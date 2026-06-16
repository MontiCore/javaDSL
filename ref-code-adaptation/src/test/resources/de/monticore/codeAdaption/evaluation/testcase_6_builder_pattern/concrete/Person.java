package de.monticore.codeAdaption.evaluation.testcase_6_builder_pattern;

/**
 * Person domain class with attributes
 */
public class Person {
    private String name;
    private int age;
    private String email;
    private String phone;

    public Person() {
    }

    public Person(String name, int age, String email, String phone) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
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

    public void sX(Object x) {}

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

