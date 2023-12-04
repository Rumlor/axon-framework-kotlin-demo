package com.rumlor.command

import com.rumlor.api.CreateFoodCartCommand
import com.rumlor.api.DeSelectProductCommand
import com.rumlor.api.SelectProductCommand
import com.rumlor.events.DeSelectedProductEvent
import com.rumlor.events.FoodCartCreatedEvent
import com.rumlor.events.SelectedProductEvent
import com.rumlor.exception.ProductDeSelectionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.AggregateRoot
import org.jboss.logging.Logger
import java.util.*

@AggregateRoot
open class FoodCartAggregateRoot()  {

    @AggregateIdentifier
    private lateinit var foodCartId: UUID

    @AggregateMember
    private lateinit var products :Set<ProductAggregateMember>

    private val logger:Logger = Logger.getLogger("FoodCartRoot")

    @CommandHandler
    constructor(command: CreateFoodCartCommand):this(){
        logger.info("create food cart command  arrived:$command")
        AggregateLifecycle.apply(FoodCartCreatedEvent(UUID.randomUUID()))
    }


    @CommandHandler
    fun on(selectProductCommand: SelectProductCommand){

        if (selectProductCommand.quantity > selectProductCommand.stock)
                throw ProductDeSelectionException("Not enough stock!!")

        logger.info("select product command arrived:$selectProductCommand")
        AggregateLifecycle.apply(SelectedProductEvent(foodCartId,selectProductCommand.productId,selectProductCommand.name,selectProductCommand.stock,selectProductCommand.quantity))
    }

    @CommandHandler
    fun on(deSelectProductCommand: DeSelectProductCommand) {
        logger.info("deselect product command arrived: $deSelectProductCommand")
        if (products.map(ProductAggregateMember::productId).contains(deSelectProductCommand.productId))
            AggregateLifecycle.apply(DeSelectedProductEvent(foodCartId,deSelectProductCommand.productId,deSelectProductCommand.quantity))
        else
            throw ProductDeSelectionException()
    }


    //handler for events generated by aggregate
    @EventSourcingHandler
    fun on(event: FoodCartCreatedEvent) {
        logger.info("food cart create event arrived: $event")
        foodCartId = event.foodCardId
        products = HashSet()
    }


    //handler for events generated by aggregate
    @EventSourcingHandler
    fun on(event: SelectedProductEvent) {
        logger.info("select product  event sourced event arrived: $event")
        products.plus(ProductAggregateMember(event.productId,event.stock,event.name,event.quantity))
    }

    @EventSourcingHandler
    fun on(event: DeSelectedProductEvent) {
        logger.info("deselect product  event sourced event arrived: $event")
        products = products.filter {
            it.productId != event.productId
        }.toSet()
    }
}