package com.rumlor.command

import com.rumlor.events.DeSelectedProductEvent
import org.axonframework.eventsourcing.EventSourcingHandler
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
        stock = stock.plus(event.quantity)

        if (event.productId != productId || quantity == 0)
            throw IllegalStateException()

        quantity = quantity.minus(event.quantity)
    }



}