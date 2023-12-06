package com.rumlor.command

import com.rumlor.api.ConfirmOrderCommand
import com.rumlor.api.CreateFoodCartCommand
import com.rumlor.api.RemoveProductCommand
import com.rumlor.api.AddProductCommand
import com.rumlor.events.ConfirmedOrderEvent
import com.rumlor.events.RemovedProductEvent
import com.rumlor.events.FoodCartCreatedEvent
import com.rumlor.events.AddedProductEvent
import com.rumlor.exception.ProductDeSelectionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.jboss.logging.Logger
import java.util.*
import kotlin.collections.HashSet
import kotlin.properties.Delegates

@AggregateRoot
open class FoodCartAggregateRoot()  {

    @AggregateIdentifier
    private lateinit var foodCartId: UUID

    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    private lateinit var products :Set<ProductAggregateMember>

    private var confirmed by Delegates.notNull<Boolean>()

    private val logger:Logger = Logger.getLogger("FoodCartRoot")


    private fun findProduct(productId:UUID):ProductAggregateMember?{
        return products.find { it.productId == productId }
    }


    @CommandHandler
    constructor(command: CreateFoodCartCommand):this(){
        logger.info("create food cart command  arrived:$command")
        AggregateLifecycle.apply(FoodCartCreatedEvent(UUID.randomUUID()))
    }

    @CommandHandler
    fun handle(addProductCommand: AddProductCommand){
        if (confirmed)
            throw IllegalStateException("Can't select product if cart is confirmed")

        if (addProductCommand.quantity > addProductCommand.stock)
                throw ProductDeSelectionException("Not enough stock!!")

        logger.info("add product command arrived:$addProductCommand")
        AggregateLifecycle.apply(AddedProductEvent(addProductCommand.productId,foodCartId,addProductCommand.productId,addProductCommand.name,addProductCommand.stock,addProductCommand.quantity))
    }
    @CommandHandler
    fun handle(command:ConfirmOrderCommand){
        logger.info("confirm cart command arrived: $command")
        AggregateLifecycle.apply(ConfirmedOrderEvent(foodCartId))
    }
    @CommandHandler
    fun handle(removeProductCommand: RemoveProductCommand) {

        if (confirmed)
            throw IllegalStateException("Can't de-select product if cart is confirmed")

        logger.info("remove product command arrived: $removeProductCommand")

        findProduct(removeProductCommand.productId)
            ?: throw ProductDeSelectionException("product not found in cart with given product id")

        AggregateLifecycle.apply(
            RemovedProductEvent(
                foodCartId,
                removeProductCommand.productId,
                removeProductCommand.quantity
            )
        )
    }
    @EventSourcingHandler
    fun on(event: FoodCartCreatedEvent) {
        logger.info("food cart create event arrived: $event")
        foodCartId = event.foodCardId
        products = HashSet()
        confirmed = false
    }
    @EventSourcingHandler
    fun on(event: AddedProductEvent) {
        logger.info("added product  event sourced event arrived: $event")
        val product = ProductAggregateMember(event.productId,event.stock,event.name,event.quantity)
        products = products.plus(product)
    }
    @EventSourcingHandler
    fun on(event: RemovedProductEvent) {
        logger.info("remove product event sourced event arrived: $event")
        val product = findProduct(productId = event.productId)!!

        if (product.isQuantityZero())
            products = products.minus(product)

    }
    @EventSourcingHandler
    fun on(event: ConfirmedOrderEvent) {
        logger.info("confirm order event sourced event arrived: $event")
        confirmed = true
    }

}