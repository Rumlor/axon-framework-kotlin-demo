package com.rumlor.resource

import com.rumlor.api.CreateFoodCartCommand
import com.rumlor.api.DeSelectProductCommand
import com.rumlor.api.SelectProductCommand
import com.rumlor.model.DeSelectedProduct
import com.rumlor.model.SelectedProduct
import com.rumlor.query.*
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
    private val queryGateway: QueryGateway) {



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
    @Path("addProduct")
    fun addProductToFoodCart(selectedProduct: SelectedProduct):Boolean{
        val view = queryGateway.query(FindProductNameAndStockQuery(UUID.fromString(selectedProduct.productId)),ProductNameAndStockView::class.java).get()
        commandGateway.send<SelectedProduct>(SelectProductCommand(
            UUID.fromString(selectedProduct.foodCartId),
            UUID.fromString(selectedProduct.productId),
            selectedProduct.quantity,
            view.stock,
            view.name))
        return true
    }

    @POST
    @Path("removeProduct")
    fun removeProductFromFoodCart(deSelectedProduct: DeSelectedProduct):Boolean{
        val view = queryGateway.query(FindProductNameAndStockQuery(UUID.fromString(deSelectedProduct.productId)),ProductNameAndStockView::class.java).get()
        commandGateway.send<DeSelectedProduct>(DeSelectProductCommand(
            UUID.fromString(deSelectedProduct.foodCartId),
            UUID.fromString(deSelectedProduct.productId),
            deSelectedProduct.quantity))
        return true
    }

}