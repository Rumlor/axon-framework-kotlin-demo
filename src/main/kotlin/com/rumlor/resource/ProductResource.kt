package com.rumlor.resource

import com.rumlor.api.CreateProductCommand
import com.rumlor.model.CreateProduct
import com.rumlor.query.FoodCartProjector
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import java.util.*

@Path("/api/product")
class ProductResource @Inject constructor(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val foodCartProjector: FoodCartProjector
){
    @POST
    @Path("create")
    fun createProduct(createProduct: CreateProduct):Boolean{
        commandGateway.send<CreateProductCommand>(CreateProductCommand(UUID.randomUUID(), createProduct.name,createProduct.stock))
        return true
    }



}