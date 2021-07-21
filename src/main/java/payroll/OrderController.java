package payroll;

import org.aspectj.weaver.ast.Or;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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
        newOrder.setStatus(Status.IN_PROGRESS);
        EntityModel<Order> orderEntityModel = assembler.toModel(repository.save(newOrder));
        return ResponseEntity
                .created(orderEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(orderEntityModel);

    }

    @PutMapping(path = "/{id}")
    ResponseEntity<?> putOrder(@RequestBody Order updatedOrder, @PathVariable Long id) {
        Order findOrder = repository.findById(id).orElseThrow(() -> new OrderNotFoundException("order with id: " + id + "was not found"));
        updatedOrder.setId(id);
        repository.save(updatedOrder);
        return ResponseEntity
                .created(assembler.toModel(updatedOrder).getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(updatedOrder);
    }

    @DeleteMapping(path = "/{id}")
    ResponseEntity<?> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
