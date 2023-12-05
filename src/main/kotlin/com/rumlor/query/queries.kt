package com.rumlor.query

import com.rumlor.domain.EventStore
import com.rumlor.domain.FoodCart
import com.rumlor.domain.FoodCartProducts
import com.rumlor.domain.Product
import com.rumlor.events.ConfirmedOrderEvent
import com.rumlor.events.DeSelectedProductEvent
import com.rumlor.events.FoodCartCreatedEvent
import com.rumlor.events.SelectedProductEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.annotation.MessageIdentifier
import org.axonframework.queryhandling.QueryHandler
import org.jboss.logging.Logger
import java.util.*


data class FindFoodCartQuery(val foodCartId:UUID)
data class FindProductNameAndStockQuery(val productID: UUID)
class RetrieveProductOptionsQuery

data class FoodCartView(val foodCartId: UUID = UUID.randomUUID(),val products:Set<ProductView> = HashSet(),val confirmed:Boolean=false)
data class ProductView(val productId:UUID = UUID.randomUUID(),val name:String? = null, val stock:Int? = null)
data class SelectedProductView(val foodCartProductsId: UUID,val foodCartId: UUID,val productID:UUID,val quantity:Int)
data class DeSelectedProductView(val foodCartId: UUID,val productID:UUID,val quantity:Int)
data class ProductNameAndStockView(val name:String,val stock: Int)

@ApplicationScoped
@Transactional
class ProductRepository@Inject constructor(
    val logger: Logger,
    val entityManager: EntityManager){

    fun findProductNameAndStock(findProductNameAndStockQuery: FindProductNameAndStockQuery):ProductNameAndStockView{
        val query = entityManager.createQuery("select new com.rumlor.query.ProductNameAndStockView(p.name,p.stock) from Product p where p.id = ?1",ProductNameAndStockView::class.java)
        query.setParameter(1,findProductNameAndStockQuery.productID.toString())
       return query.resultList[0]
    }
}

@ApplicationScoped
@Transactional
class FoodCartRepository @Inject constructor(
    val logger: Logger,
    val entityManager: EntityManager){

    fun save(foodCartView: FoodCartView,messageIdentifier: String) {
        val event = entityManager.find(EventStore::class.java,messageIdentifier)
        if (event == null)
            entityManager.find(FoodCart::class.java,foodCartView.foodCartId.toString()).let {
                if (it == null) entityManager.persist(FoodCart.from(foodCartView))
                entityManager.persist(EventStore(UUID.fromString(messageIdentifier)))
            }
    }

    fun save(selectedProduct: SelectedProductView,messageIdentifier: String) {

        val event = entityManager.find(EventStore::class.java,messageIdentifier)

        if (event == null){
            val foodCart = entityManager.createQuery("select f from  FoodCart f where f.id = ?1 ",FoodCart::class.java)
                .setParameter(1,selectedProduct.foodCartId.toString())
                .resultList[0]!!

            val foodCartProducts =  foodCart.foodCartProducts.find {
                it.product?.id == selectedProduct.productID.toString()
            }

            if (foodCartProducts != null){
                foodCartProducts.quantity = foodCartProducts.quantity.plus(selectedProduct.quantity)
            } else
                foodCart.foodCartProducts = foodCart.foodCartProducts.plus(FoodCartProducts(selectedProduct.quantity,foodCart,entityManager.find(Product::class.java,selectedProduct.productID.toString())))
            entityManager.find(Product::class.java,selectedProduct.productID.toString())?.let {
                it.stock = it.stock?.minus(selectedProduct.quantity)
            }
            entityManager.persist(EventStore(UUID.fromString(messageIdentifier)))
        }


    }

    fun find(uuid: UUID):FoodCartView =
        entityManager.find(FoodCart::class.java,uuid).let {
        FoodCartView(UUID.fromString(it.id),it.foodCartProducts.map { foodCartProducts ->
            ProductView(
                name = foodCartProducts.product?.name,
                stock = foodCartProducts.product?.stock
            )
        }.toSet())
    }

    fun save(deSelectedProductView: DeSelectedProductView,messageIdentifier: String) {

        val event = entityManager.find(EventStore::class.java,messageIdentifier)

        if (event == null){
            val foodCart = entityManager.createQuery("select f from  FoodCart f where f.id = ?1 ",FoodCart::class.java)
                .setParameter(1,deSelectedProductView.foodCartId)
                .resultList[0]!!

            val foodCartProducts =  foodCart.foodCartProducts.find {
                it.product?.id == deSelectedProductView.productID.toString()
            }

            if (foodCartProducts != null){
                foodCartProducts.quantity = foodCartProducts.quantity.minus(deSelectedProductView.quantity)
            } else
                foodCart.foodCartProducts = foodCart.foodCartProducts.minus(FoodCartProducts(deSelectedProductView.quantity,foodCart,entityManager.find(Product::class.java,deSelectedProductView.productID)))
            entityManager.find(Product::class.java,deSelectedProductView.productID)?.let {
                it.stock = it.stock?.plus(deSelectedProductView.quantity)
            }
            entityManager.persist(EventStore(UUID.fromString(messageIdentifier)))
        }


    }

    fun save(event: ConfirmedOrderEvent,messageIdentifier: String) {
        val eventStore = entityManager.find(EventStore::class.java,messageIdentifier)

        if (eventStore == null){

            entityManager.find(FoodCart::class.java,event.foodCardId.toString())?.let {
                it.confirmed = true
            }

        }
    }


}

@ApplicationScoped
class FoodCartProjector @Inject constructor(
    val foodCartRepository: FoodCartRepository,
    val productRepository: ProductRepository,
    val logger:Logger){

    @EventHandler
    fun on(foodCartCreatedEvent: FoodCartCreatedEvent,@MessageIdentifier messageIdentifier:String){
        logger.info("food cart created event arrived:$foodCartCreatedEvent")
        foodCartRepository.save(FoodCartView(foodCartId =  foodCartCreatedEvent.foodCardId),messageIdentifier)
    }

    @EventHandler
    fun on(selectedProductEvent: SelectedProductEvent,@MessageIdentifier messageIdentifier: String){
        logger.info("selected product event arrived:$selectedProductEvent")
        foodCartRepository.save(SelectedProductView(selectedProductEvent.foodCartProductsId,selectedProductEvent.foodCardId,selectedProductEvent.productId,selectedProductEvent.quantity),messageIdentifier)
    }

    @EventHandler
    fun on(event: ConfirmedOrderEvent,@MessageIdentifier messageIdentifier: String){
        logger.info("confirm order event arrived:$event")
        foodCartRepository.save(event,messageIdentifier)
    }
    @EventHandler
    fun on(deSelectedProductEvent: DeSelectedProductEvent,@MessageIdentifier messageIdentifier: String){
        logger.info("de-selected product event arrived:$deSelectedProductEvent")
        foodCartRepository.save(DeSelectedProductView(deSelectedProductEvent.foodCardId,deSelectedProductEvent.productId,deSelectedProductEvent.quantity),messageIdentifier)
    }
    @QueryHandler
    fun on(findFoodCartQuery: FindFoodCartQuery): FoodCartView {
        logger.info("find food cart query arrived: $findFoodCartQuery")
        val uuid = findFoodCartQuery.foodCartId
        return foodCartRepository.find(uuid)
    }


    @QueryHandler
    fun on(findProductNameAndStockQuery: FindProductNameAndStockQuery): ProductNameAndStockView {
        logger.info("find product name and stock query arrived: $findProductNameAndStockQuery")
        return productRepository.findProductNameAndStock(findProductNameAndStockQuery)
    }

}