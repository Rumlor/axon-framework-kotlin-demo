package com.rumlor.query

import com.rumlor.command.FoodCart
import com.rumlor.events.FoodCartCreatedEvent
import com.rumlor.events.SelectedProductEvent
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.axonframework.eventhandling.EventHandler
import org.axonframework.modelling.command.Aggregate
import org.axonframework.modelling.command.Repository
import org.axonframework.queryhandling.QueryHandler
import org.jboss.logging.Logger
import java.util.HashMap
import java.util.UUID


data class FindFoodCartQuery(val foodCartId:UUID)

class RetrieveProductOptionsQuery

data class FoodCartView(val foodCartId: UUID,val products:HashMap<UUID,Int>)


@ApplicationScoped
class FoodCartRepository @Inject constructor(val dataSource: AgroalDataSource){

    fun save(foodCartView: FoodCartView) {

        val uuid = foodCartView.foodCartId
        with(dataSource.connection){

            autoCommit = false
            val nextRow = prepareStatement("""
                SELECT id
                FROM footcartdemo.foot_cart
                WHERE id='${uuid}';
            """.trimIndent()).executeQuery().next()
            if (!nextRow) {
                createStatement().execute(
                    """
            INSERT INTO footcartdemo.foot_cart
            (id)
            VALUES('$uuid');
            """.trimIndent()
                )

                foodCartView.products.forEach {
                    createStatement().execute(
                        """
                INSERT INTO footcartdemo.foot_cart_products (id, quantity, foot_card) VALUES('${it.key}', ${it.value}, '$uuid');
                """.trimIndent()
                    )
                }


            }
            commit()
        }


    }


    fun find(uuid: UUID):FoodCartView =
        with(dataSource.connection){
          var rs =  prepareStatement("""
                SELECT id, quantity, foot_card
                FROM footcartdemo.foot_cart_products WHERE foot_card = '$uuid';
            """.trimIndent()).executeQuery()
          val products:HashMap<UUID,Int> = HashMap()
          while (rs.next()){
              products.merge(UUID.fromString(rs.getString("id")),rs.getInt("quantity")){
                  v1,v2->
                  v1.plus(v2)
              }
          }
          rs =  prepareStatement("""
                SELECT id
                FROM footcartdemo.foot_cart WHERE id = '$uuid';
            """.trimIndent()).executeQuery()
          return FoodCartView(uuid,products)
        }

}

@ApplicationScoped
class FoodCartProjector @Inject constructor(
    val aggregateRepository: Repository<FoodCart>,
    val logger:Logger){

    @EventHandler
    fun on(foodCartCreatedEvent: FoodCartCreatedEvent){
        logger.info("food cart created event arrived:$foodCartCreatedEvent")
        val foodCartView = FoodCartView(foodCartCreatedEvent.foodCardId, HashMap())
    }

    @EventHandler
    fun on(foodCartCreatedEvent: SelectedProductEvent){
        logger.info("selected product event arrived:$foodCartCreatedEvent")
    }

    @QueryHandler
    fun on(findFoodCartQuery: FindFoodCartQuery): FoodCartView {
        logger.info("find food cart query arrived: $findFoodCartQuery")
        val uuid = findFoodCartQuery.foodCartId
        val aggregate:Aggregate<FoodCart> = aggregateRepository.load(uuid.toString())
        return aggregate.invoke{
            FoodCartView(it.foodCartId,it.selectedProducts)
        }
    }


}