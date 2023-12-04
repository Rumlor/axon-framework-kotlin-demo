package com.rumlor.command

import com.rumlor.api.SelectProductCommand
import com.rumlor.events.DeSelectedProductEvent
import com.rumlor.events.SelectedProductEvent
import com.rumlor.exception.InvalidProductStockException
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


    @EventSourcingHandler
    fun on(event: DeSelectedProductEvent) {
        logger.info("deselect product event sourced event arrived: $event")
        stock.plus(event.quantity)
        quantity.minus(event.quantity)
    }

    @CommandHandler
    fun on(selectProductCommand: SelectProductCommand){
        logger.info("select product command arrived:$selectProductCommand")
        if (selectProductCommand.quantity > stock)
            throw InvalidProductStockException()
        AggregateLifecycle.apply(SelectedProductEvent(UUID.randomUUID(),selectProductCommand.foodCardId,productId,name,stock,quantity))
    }

    @EventSourcingHandler
    fun on(event: SelectedProductEvent) {
        logger.info("select product  event sourced event arrived: $event")
        this.stock = this.stock.minus(event.quantity)
        this.quantity = this.quantity.plus(event.quantity)
    }


}