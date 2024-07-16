package kz.runtime.spring.controller;

import kz.runtime.spring.pojo.Human;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/test_controller")
public class TestController {

    @GetMapping(path = "/resource_1") // http://localhost:8080/test_controller/resource_1?name=D&age=15
    public String firstResource(@RequestParam(name = "name", required = true) String name,
                                @RequestParam(name = "age", required = false) Integer age) {
        String result = """
                <p>Name: <b>%s</b></p>
                <p>Age: <b>%d</b></p>
                """;
        return result.formatted(name, age);
    }

    @GetMapping(path = "/resource_2", produces = "text/plain") // http://localhost:8080/test_controller/resource_2
    public String secondResource(@RequestParam(name = "from", required = false) Integer from,
                                 @RequestParam(name = "to", required = false) Integer to) {

        List<Human> humanList = createPeople();
        StringBuilder result = new StringBuilder();


        if (from == null) from = Integer.MIN_VALUE;
        if (to == null) to = Integer.MAX_VALUE;

        for (Human human : humanList) {
            if (human.getAge() >= from && human.getAge() <= to) {
                result.append(human.getName());
                result.append(", ");
                result.append(human.getAge());
                result.append(" y.o.\n");
            }
        }

        return result.toString();
    }

    @GetMapping(path = "/resource_3", produces = "application/json") // http://localhost:8080/test_controller/resource_3
    public List<Human> thirdResource(@RequestParam(name = "city", required = false) String city) {

        List<Human> humanList = createPeople();
        List<Human> resultList = new ArrayList<>();

        for (Human human : humanList) {
            if (city == null || human.getCity().equals(city)) {
                resultList.add(human);
            }
        }

        return resultList;
    }

    @GetMapping(path = "/resource_test", produces = "image/png") // http://localhost:8080/test_controller/resource_test
    public byte[] testResource() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("C:\\Users\\runtime-809b-3\\Pictures\\Снимок.PNG"));
        return bytes;
    }

    public List<Human> createPeople() {
        List<Human> people = new ArrayList<>();

        people.add(new Human("Mark", 10, "Astana"));
        people.add(new Human("Tom", 22, "Almaty"));
        people.add(new Human("Milly", 79, "Semey"));
        people.add(new Human("Holly", 56, "Astana"));
        people.add(new Human("Jake", 28, "Almaty"));
        people.add(new Human("Angela", 31, "Atyrau"));

        return people;
    }
}