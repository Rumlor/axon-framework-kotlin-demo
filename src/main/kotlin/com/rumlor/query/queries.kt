package com.rumlor.query

import com.rumlor.command.FoodCartAggregateRoot
import com.rumlor.events.FoodCartCreatedEvent
import com.rumlor.events.SelectedProductEvent
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.axonframework.eventhandling.EventHandler
import org.axonframework.modelling.command.Repository
import org.axonframework.queryhandling.QueryHandler
import org.jboss.logging.Logger
import java.util.*


data class FindFoodCartQuery(val foodCartId:UUID)

class RetrieveProductOptionsQuery

data class FoodCartView(val foodCartId: UUID,val products:HashMap<UUID,Int>)
data class SelectedProductView(val foodCartId: UUID,val productId: UUID,val quantity:Int)

@ApplicationScoped
class FoodCartRepository @Inject constructor(
    val logger: Logger,
    val dataSource: AgroalDataSource){

    fun save(foodCartView: FoodCartView) {

        val uuid = foodCartView.foodCartId
        with(dataSource.connection){

            autoCommit = false
            var query = """
                SELECT id
                FROM footcartdemo.foot_cart
                WHERE id='${uuid}';
            """
            println(query)

            val nextRow = prepareStatement(query.trimIndent()).executeQuery().next()
            if (!nextRow) {

                query = """
                INSERT INTO footcartdemo.foot_cart
                (id)
                VALUES('$uuid');
                """
                println(query)

                createStatement().execute(
                query .trimIndent()
                )

                val productQuery = { entry:Map.Entry<UUID,Int>->
                    """
                INSERT INTO footcartdemo.foot_cart_products (id, quantity, foot_card) VALUES('${entry.key}', ${entry.value}, '$uuid');
                """
                }
                println(query)
                foodCartView.products.forEach {
                    createStatement().execute(
                     productQuery.invoke(it) .trimIndent()
                    )
                }


            }
            commit()
        }


    }

    fun save(selectedProductView: SelectedProductView) {
        with(dataSource.connection){
            autoCommit = false
            var query = """
               SELECT id, quantity, foot_card
               FROM footcartdemo.foot_cart_products WHERE id = '${selectedProductView.productId}' and foot_card='${selectedProductView.foodCartId}'
            """.trimIndent()
            println(query)
            val rs = createStatement().executeQuery(query)

            if (!rs.next()){
                query = """
                 INSERT INTO footcartdemo.foot_cart_products (id, quantity, foot_card) VALUES('${selectedProductView.productId}', ${selectedProductView.quantity}, '${selectedProductView.foodCartId}');
                """
                println(query)
                createStatement().execute(
                    query.trimIndent())
            }else{

                val currentCount = rs.getInt("quantity")
                val id = rs.getString("id")

                query = """
                    UPDATE footcartdemo.foot_cart_products
                    SET quantity=${currentCount + selectedProductView.quantity}
                    WHERE id = '$id';
                """.trimIndent()

            }

            commit()
        }

    }

    fun find(uuid: UUID):FoodCartView =
        with(dataSource.connection){
            var query = """
                SELECT id, quantity, foot_card
                FROM footcartdemo.foot_cart_products WHERE foot_card = '$uuid';
            """
            var rs =  prepareStatement(query.trimIndent()).executeQuery()
            val products:HashMap<UUID,Int> = HashMap()

            while (rs.next()){
                products.merge(UUID.fromString(rs.getString("id")),rs.getInt("quantity")){
                        v1,v2->
                    v1.plus(v2)
                }
            }

            query = """
                SELECT id
                FROM footcartdemo.foot_cart WHERE id = '$uuid';
            """
            rs =  prepareStatement(query.trimIndent()).executeQuery()
            return FoodCartView(uuid,products)
        }

}




@ApplicationScoped
class FoodCartProjector @Inject constructor(
    val foodCartRepository: FoodCartRepository,
    val aggregateRepository: Repository<FoodCartAggregateRoot>,
    val logger:Logger){

    @EventHandler
    fun on(foodCartCreatedEvent: FoodCartCreatedEvent){
        logger.info("food cart created event arrived:$foodCartCreatedEvent")
        val foodCartView = FoodCartView(foodCartCreatedEvent.foodCardId, HashMap())
        foodCartRepository.save(foodCartView)
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