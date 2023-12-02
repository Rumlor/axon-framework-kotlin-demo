package com.rumlor.query

import com.rumlor.domain.FoodCart
import com.rumlor.domain.FoodCartProducts
import com.rumlor.domain.Product
import com.rumlor.events.FoodCartCreatedEvent
import com.rumlor.events.SelectedProductEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.jboss.logging.Logger
import java.util.*


data class FindFoodCartQuery(val foodCartId:UUID)

class RetrieveProductOptionsQuery

data class FoodCartView(val foodCartId: UUID = UUID.randomUUID(),val products:Set<ProductView> = HashSet(),val confirmed:Boolean=false)
data class ProductView(val productId:UUID = UUID.randomUUID(),val name:String? = null, val stock:Int? = null)
data class SelectedProductView(val foodCartId: UUID,val productID:UUID,val quantity:Int)
@ApplicationScoped
class FoodCartRepository @Inject constructor(
    val logger: Logger,
    val entityManager: EntityManager){

    @Transactional
    fun save(foodCartView: FoodCartView) {
        entityManager.persist(FoodCart.from(foodCartView))
    }

    @Transactional
    fun save(selectedProduct: SelectedProductView) {
        entityManager.find(FoodCart::class.java,selectedProduct.foodCartId).let {
            val foodCartProducts = FoodCartProducts(selectedProduct.quantity)
            foodCartProducts.product = entityManager.find(Product::class.java,selectedProduct.productID)
            it.foodCartProducts.plus(foodCartProducts)
        }
    }

    fun find(uuid: UUID):FoodCartView = entityManager.find(FoodCart::class.java,uuid).let {
        FoodCartView(it.id,it.foodCartProducts.map { foodCartProducts ->
            ProductView(foodCartProducts.product?.id ?: UUID.randomUUID(),
                        foodCartProducts.product?.name,
                        foodCartProducts.product?.stock)
        }.toSet())
    }


}




@ApplicationScoped
class FoodCartProjector @Inject constructor(
    val foodCartRepository: FoodCartRepository,
    val logger:Logger){

    @EventHandler
    fun on(foodCartCreatedEvent: FoodCartCreatedEvent){
        logger.info("food cart created event arrived:$foodCartCreatedEvent")
        foodCartRepository.save(FoodCartView(foodCartId =  foodCartCreatedEvent.foodCardId))
    }

    @EventHandler
    fun on(selectedProductEvent: SelectedProductEvent){
        logger.info("selected product event arrived:$selectedProductEvent")
        foodCartRepository.save(SelectedProductView(selectedProductEvent.foodCardId,selectedProductEvent.productId,selectedProductEvent.quantity))
    }

    @QueryHandler
    fun on(findFoodCartQuery: FindFoodCartQuery): FoodCartView {
        logger.info("find food cart query arrived: $findFoodCartQuery")
        val uuid = findFoodCartQuery.foodCartId
        return foodCartRepository.find(uuid)
    }


}