package de.thi.informatik.edi.example01;

import org.springframework.stereotype.Component;

import java.util.List;

// Strg + Leertaste ist Autocomplete
@Component
public class ExampleComponent {

    private ProductRepository repo;

    public ExampleComponent(ProductRepository repo) {
        this.repo = repo;
    }

    public void doSomething() {
        System.out.println("Test");
    }

    public Product createProduct() {
        Product p = new Product();
        p.setName("Example");
        p.setPrice(1.99);
        System.out.println(repo);

        repo.save(p);
        return p;
    }

    public List<Product> listProducts() {
        return repo.findByPriceGreaterThan(1.0);
    }
}
