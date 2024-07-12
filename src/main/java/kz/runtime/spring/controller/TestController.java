package kz.runtime.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping(path = "/test_controller")
public class TestController {

    @GetMapping(path = "/resource_1", produces = "image/png") // http://localhost:8080/test_controller/resource_1
    public byte[] firstResource() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("C:\\Users\\runtime-809b-3\\Pictures\\Снимок.PNG"));
        return bytes;
    }
}