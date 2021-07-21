package payroll;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository repository;
    private final OrderModelAssembler assembler;

    public OrderController(OrderRepository repository, OrderModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    // get all orders
    @GetMapping(path = "")
    ResponseEntity<?> all() {
        List<EntityModel<Order>> orders = repository.findAll().stream().map(assembler::toModel).collect(Collectors.toList());
        return ResponseEntity
                .ok(CollectionModel.of(orders, linkTo(methodOn(OrderController.class).all()).withRel("orders")));
    }

    @GetMapping(path = "/{id}")
    EntityModel<Order> one(@PathVariable Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("order with id: " + id + " not found"));
        return assembler.toModel(order);

    }

    @PostMapping(path = "")
    ResponseEntity<?> postOrder(@RequestBody Order newOrder) {
        EntityModel<Order> orderEntityModel = assembler.toModel(repository.save(newOrder));
        return ResponseEntity
                .created(orderEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(orderEntityModel);

    }

}
