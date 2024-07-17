package kz.runtime.spring.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // создает геттеры на этапе компиляции
// @RequiredArgsConstructor // создает конструктор для всех обязательных(final) полей
@AllArgsConstructor
public class Human {
    private String name;
    private Integer age;
    private String city;

    public Human(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    /*public Human(String name, Integer age, String city) {
        this.name = name;
        this.age = age;
        this.city = city;
    }*/

    /*public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }*/
}