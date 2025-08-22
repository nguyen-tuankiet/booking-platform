package com.booking.api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/routes")
    public ResponseEntity<List<String>> getAllRoutes() {
        Flux<Route> routes = routeLocator.getRoutes();

        List<String> routeInfo = routes
                .map(route -> String.format("ID: %s, URI: %s, Predicate: %s",
                        route.getId(),
                        route.getUri().toString(),
                        route.getPredicate().toString()))
                .collectList()
                .block();

        return ResponseEntity.ok(routeInfo);
    }
}