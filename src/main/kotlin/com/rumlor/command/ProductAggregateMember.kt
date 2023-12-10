package com.rumlor.command

import com.rumlor.api.ChangeFoodCartProductQuantityCommand
import com.rumlor.api.RemoveProductDeductQuantityCommand
import com.rumlor.events.AddedProductEvent
import com.rumlor.events.ChangeQuantityEvent
import com.rumlor.events.RemovedProductAppliedEvent
import com.rumlor.exception.ProductDeSelectionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.EntityId
import org.jboss.logging.Logger
import java.util.*

data class ProductAggregateMember(
    @EntityId
    var productId: UUID,
    var stock :Int,
    var name:String,
    var quantity:Int
) {

    private val logger: Logger = Logger.getLogger("ProductMember")

    fun isQuantityZero():Boolean{
        return quantity == 0
    }

    @CommandHandler
    fun handle(removeProductCommand: RemoveProductDeductQuantityCommand) {

        logger.info("remove product command arrived: $removeProductCommand")

        if (removeProductCommand.quantity <= 0){
            throw ProductDeSelectionException("can not remove zero or less product")
        }

        else if (removeProductCommand.quantity > quantity)
            throw ProductDeSelectionException("can not remove more product than what is already in cart")

        AggregateLifecycle.apply(
            RemovedProductAppliedEvent(
                removeProductCommand.foodCartId,
                removeProductCommand.productId,
                removeProductCommand.quantity
            )
        )
    }
    @CommandHandler
    fun handle(command: ChangeFoodCartProductQuantityCommand){
        logger.info("changed food cart product quantity command arrived:$command")
        val diff = command.newQuantity.minus(quantity)
        if (diff > stock)
            throw IllegalStateException("quantity can't be higher than stock")

        AggregateLifecycle.apply(ChangeQuantityEvent(command.productId,command.foodCartId,command.newQuantity))
    }
    @EventSourcingHandler
    fun on(event: AddedProductEvent) {
        logger.info("added product event sourced event arrived: $event")
        stock = stock.minus(quantity)
    }
    @EventSourcingHandler
    fun on(event: RemovedProductAppliedEvent) {
        logger.info("remove product applied event sourced event arrived: $event")

        stock = stock.plus(event.quantity)
        quantity = quantity.minus(event.quantity)
    }
    @EventSourcingHandler
    fun on(event: ChangeQuantityEvent){
        logger.info("changed food cart product quantity event arrived:$event")
        val diff = quantity.minus(event.quantity)
        quantity = event.quantity
        stock = stock.plus(diff)
    }



}