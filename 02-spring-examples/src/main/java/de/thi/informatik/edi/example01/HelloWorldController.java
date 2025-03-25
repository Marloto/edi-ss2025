package de.thi.informatik.edi.example01;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foo")
public class HelloWorldController {
    private final ExampleComponent component;

    public HelloWorldController(ExampleComponent component) {
        this.component = component;
    }

    @GetMapping("/hello")
    public String sayHello() {
        System.out.println(component);
        return "Hello, World 1!";
    }

    @GetMapping("/hello/{name}")
    public String sayHello3(@PathVariable String name) {
        return "Hello, " + name;
    }

    @PostMapping("/hello")
    public String sayHello2(@RequestBody Message message) {
        return message.getMsg();
    }



    @GetMapping("/product")
    public Product createProd() {
        Product p = component.createProduct();
        return p;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return component.listProducts();
    }




}
