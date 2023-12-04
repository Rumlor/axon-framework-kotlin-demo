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
data class FindProductNameAndStockQuery(val productID: UUID)
class RetrieveProductOptionsQuery

data class FoodCartView(val foodCartId: UUID = UUID.randomUUID(),val products:Set<ProductView> = HashSet(),val confirmed:Boolean=false)
data class ProductView(val productId:UUID = UUID.randomUUID(),val name:String? = null, val stock:Int? = null)
data class SelectedProductView(val foodCartId: UUID,val productID:UUID,val quantity:Int)
data class ProductNameAndStockView(val name:String,val stock: Int)

@ApplicationScoped
@Transactional
class ProductRepository@Inject constructor(
    val logger: Logger,
    val entityManager: EntityManager){

    fun findProductNameAndStock(findProductNameAndStockQuery: FindProductNameAndStockQuery):ProductNameAndStockView{
        val query = entityManager.createQuery("select new com.rumlor.query.ProductNameAndStockView(p.name,p.stock) from Product p where p.id = ?1",ProductNameAndStockView::class.java)
        query.setParameter(1,findProductNameAndStockQuery.productID)
       return query.resultList[0]
    }
}

@ApplicationScoped
class FoodCartRepository @Inject constructor(
    val logger: Logger,
    val entityManager: EntityManager){

    @Transactional
    fun save(foodCartView: FoodCartView) {
        entityManager.find(FoodCart::class.java,foodCartView.foodCartId).let {
            if (it == null) entityManager.persist(FoodCart.from(foodCartView))
        }
    }

    @Transactional
    fun save(selectedProduct: SelectedProductView) {

        val foodCart = entityManager.createQuery("select f from  FoodCart f where f.id = ?1 ",FoodCart::class.java)
            .setParameter(1,selectedProduct.foodCartId)
            .resultList[0]!!

        val foodCartProducts =  foodCart.foodCartProducts.find {
            it.product?.id == selectedProduct.productID
        }

        if (foodCartProducts != null){
            foodCartProducts.quantity = foodCartProducts.quantity.plus(selectedProduct.quantity)
        } else
            foodCart.foodCartProducts = foodCart.foodCartProducts.plus(FoodCartProducts(selectedProduct.quantity,foodCart,entityManager.find(Product::class.java,selectedProduct.productID)))

    }

    @Transactional
    fun find(uuid: UUID):FoodCartView =
        entityManager.find(FoodCart::class.java,uuid).let {
        FoodCartView(it.id,it.foodCartProducts.map { foodCartProducts ->
            ProductView(
                name = foodCartProducts.product?.name,
                stock = foodCartProducts.product?.stock
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


    @QueryHandler
    fun on(findProductNameAndStockQuery: FindProductNameAndStockQuery): ProductNameAndStockView {
        logger.info("find product name and stock query arrived: $findProductNameAndStockQuery")
        return productRepository.findProductNameAndStock(findProductNameAndStockQuery)
    }

}