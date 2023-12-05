package com.rumlor.command

import com.rumlor.api.ChangeFoodCartProductQuantityCommand
import com.rumlor.events.AddedProductEvent
import com.rumlor.events.ChangeQuantityEvent
import com.rumlor.events.RemovedProductEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.EntityId
import org.jboss.logging.Logger
import java.util.*

data class ProductAggregateMember(
    @EntityId
    var productId: UUID,
    private var stock :Int,
    private  var name:String,
    private var quantity:Int
) {

    private val logger: Logger = Logger.getLogger("ProductMember")

    @CommandHandler
    fun on(command: ChangeFoodCartProductQuantityCommand){
        logger.info("changed food cart product quantity command arrived:$command")

        if (command.newQuantity > stock)
            throw IllegalStateException("quantity can't be higher than stock")

        AggregateLifecycle.apply(ChangeQuantityEvent(command.productId,command.foodCartId,command.newQuantity))
    }

    @EventSourcingHandler
    fun on(event: ChangeQuantityEvent){
        logger.info("changed food cart product quantity event arrived:$event")
        val diff = quantity.minus(event.quantity)
        quantity = event.quantity
        stock = stock.plus(diff)
    }


}