package com.rumlor.resource

import com.rumlor.api.CreateFoodCartCommand
import com.rumlor.api.SelectProductCommand
import com.rumlor.model.SelectedProduct
import com.rumlor.query.FindFoodCartQuery
import com.rumlor.query.FoodCartProjector
import com.rumlor.query.FoodCartView
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import java.util.*
import java.util.concurrent.CompletableFuture

@Path("/api/footcart")
class FoodCartResource @Inject constructor(
    private val commandGateway:CommandGateway,
    private val queryGateway: QueryGateway,
    private val foodCartProjector: FoodCartProjector) {



    @GET
    @Path("{uuid}")
    fun findFoodCart(@PathParam("uuid") foodCartId:String):CompletableFuture<FoodCartView> =
        queryGateway.query(FindFoodCartQuery(UUID.fromString(foodCartId)), FoodCartView::class.java)



    @POST
    @Path("create")
    fun createFoodCart():Boolean{
        commandGateway.send<CreateFoodCartCommand>(CreateFoodCartCommand())
        return true
    }

    @POST
    @Path("select")
    fun selectProduct(selectedProduct: SelectedProduct):Boolean{
        commandGateway.send<SelectProductCommand>(
            SelectProductCommand(
                UUID.fromString(selectedProduct.foodCartId),
                UUID.randomUUID(),
                selectedProduct.quantity))
        return true
    }

}