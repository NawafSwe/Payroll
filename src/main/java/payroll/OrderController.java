package payroll;

import org.aspectj.weaver.ast.Or;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        newOrder.setStatus(Status.IN_PROGRESS);
        EntityModel<Order> orderEntityModel = assembler.toModel(repository.save(newOrder));
        return ResponseEntity
                .created(orderEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(orderEntityModel);

    }

    @PutMapping(path = "/{id}")
    ResponseEntity<?> complete(@PathVariable Long id) {
        Order findOrder = repository.findById(id).orElseThrow(() -> new OrderNotFoundException("order with id: " + id + "was not found"));

        if (findOrder.getStatus() == Status.IN_PROGRESS) {
            findOrder.setStatus(Status.COMPLETED);
            EntityModel<Order> updatedEntityOrder = assembler.toModel(repository.save(findOrder));
            return ResponseEntity
                    .created(updatedEntityOrder.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(updatedEntityOrder);
        }

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(
                        Problem.create()
                                .withTitle("Method Not Allowed")
                                .withDetail("You Cannot change status of this order from " + findOrder.getStatus()));
    }

    @DeleteMapping(path = "/{id}")
    ResponseEntity<?> cancel(@PathVariable Long id) {
        Order order = repository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException("Not found"));

        if (order.getStatus() == Status.IN_PROGRESS) {
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(assembler.toModel(repository.save(order)));
        }
        return ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't cancel an order that is in the " + order.getStatus() + " status"));
    }
}

