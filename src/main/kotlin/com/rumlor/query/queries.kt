package com.rumlor.query

import com.rumlor.domain.EventStore
import com.rumlor.domain.FoodCart
import com.rumlor.domain.FoodCartProducts
import com.rumlor.domain.Product
import com.rumlor.events.*
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

data class ChangeQuantityView(val foodCardId: UUID, val productId: UUID, val quantity: Int)

data class FoodCartView(val foodCartId: UUID = UUID.randomUUID(),val products:Set<FoodCartProductView> = HashSet(),val confirmed:Boolean=false)
data class ProductView(val productId:UUID = UUID.randomUUID(),val name:String? = null, val stock:Int? = null)
data class FoodCartProductView(val productId:UUID = UUID.randomUUID(),val name:String? = null, val quantity:Int? = null)
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

    fun save(deSelectedProductView: DeSelectedProductView,messageIdentifier: String) {

        val event = entityManager.find(EventStore::class.java,messageIdentifier)

        if (event == null){

            val foodCart: FoodCart = entityManager.find(FoodCart::class.java,deSelectedProductView.foodCartId.toString())
                ?: throw IllegalStateException("no food cart found with given food cart id")

            val foodCartProducts =  foodCart.foodCartProducts.find {
                it.product?.id == deSelectedProductView.productID.toString()
            }

            if (foodCartProducts != null){
                foodCartProducts.quantity = foodCartProducts.quantity.minus(deSelectedProductView.quantity)
            } else
                foodCart.foodCartProducts = foodCart.foodCartProducts.minus(FoodCartProducts(deSelectedProductView.quantity,foodCart,entityManager.find(Product::class.java,deSelectedProductView.productID.toString())))
            entityManager.find(Product::class.java,deSelectedProductView.productID.toString())?.let {
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
            entityManager.persist(EventStore(UUID.fromString(messageIdentifier)))
        }
    }

    fun save(changeQuantityView: ChangeQuantityView, messageIdentifier: String) {
        val eventStore = entityManager.find(EventStore::class.java,messageIdentifier)

        if (eventStore == null) {
            entityManager.find(FoodCart::class.java,changeQuantityView.foodCardId.toString())?.let {
                val foodCartProducts:FoodCartProducts? = it.foodCartProducts.find {
                    foodCartProducts -> foodCartProducts.product?.id == changeQuantityView.productId.toString()
                }
                if (foodCartProducts != null){
                    val diff = foodCartProducts.quantity.minus(changeQuantityView.quantity)
                    foodCartProducts.quantity = changeQuantityView.quantity
                    entityManager.find(Product::class.java,changeQuantityView.productId.toString())?.let {
                       product->
                        product.stock = product.stock?.plus(diff)
                    }
                }
            }
            entityManager.persist(EventStore(UUID.fromString(messageIdentifier)))
        }

    }

    fun find(uuid: UUID):FoodCartView? =
        entityManager.find(FoodCart::class.java,uuid.toString())?.let {
            FoodCartView(UUID.fromString(it.id),it.foodCartProducts.map { foodCartProducts ->
                FoodCartProductView(
                    name = foodCartProducts.product?.name,
                    quantity = foodCartProducts.quantity
                )
            }.toSet())
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
    fun on(addedProductEvent: AddedProductEvent, @MessageIdentifier messageIdentifier: String){
        logger.info("selected product event arrived:$addedProductEvent")
        foodCartRepository.save(SelectedProductView(addedProductEvent.foodCartProductsId,addedProductEvent.foodCardId,addedProductEvent.productId,addedProductEvent.quantity),messageIdentifier)
    }

    @EventHandler
    fun on(event: ConfirmedOrderEvent,@MessageIdentifier messageIdentifier: String){
        logger.info("confirm order event arrived:$event")
        foodCartRepository.save(event,messageIdentifier)
    }

    @EventHandler
    fun on(changeQuantityEvent: ChangeQuantityEvent, @MessageIdentifier messageIdentifier: String){
        logger.info("change product quantity event arrived:$changeQuantityEvent")
        foodCartRepository.save(ChangeQuantityView(changeQuantityEvent.foodCardId,changeQuantityEvent.productId,changeQuantityEvent.quantity),messageIdentifier)
    }
    @EventHandler
    fun on(removedProductEvent: RemovedProductAppliedEvent, @MessageIdentifier messageIdentifier: String){
        logger.info("de-selected product event arrived:$removedProductEvent")
        foodCartRepository.save(DeSelectedProductView(removedProductEvent.foodCardId,removedProductEvent.productId,removedProductEvent.quantity),messageIdentifier)
    }
    @QueryHandler
    fun on(findFoodCartQuery: FindFoodCartQuery): FoodCartView? {
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
