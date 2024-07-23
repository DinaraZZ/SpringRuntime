package kz.runtime.spring.controller;

import kz.runtime.spring.pojo.Human;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path = "/view_controller")
public class ViewController {

    @GetMapping(path = "/resource_1")
    public String firstResource(Model model) {
        Human human = new Human("Mark", 14, "Astana");
//        String msg = "Message from ViewController.firstResource()";
        model.addAttribute("human", human);
        return "view_resource_1_page";
    }

    @GetMapping(path = "resource_2")
    public String secondResource(@RequestParam(name = "city", required = false) String city,
                                 Model model) {
        Human[] humans = new Human[]{
                new Human("Mark", 10, "Astana"),
                new Human("Tom", 22, "Almaty"),
                new Human("Milly", 79, "Semey")
        };

        if (city != null) {
            List<Human> humansList = new ArrayList<>();
            for (Human human : humans) {
                if (human.getCity().equals(city)) {
                    humansList.add(human);
                }
            }
            model.addAttribute("humans", humansList);
            model.addAttribute("city", city);
        } else {
            model.addAttribute("humans", humans);
        }

        return "view_resource_2_page";
    }
}