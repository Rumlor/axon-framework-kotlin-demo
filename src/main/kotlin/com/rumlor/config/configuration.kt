package com.rumlor.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rumlor.command.FoodCart
import com.rumlor.query.FoodCartProjector
import io.quarkus.runtime.Startup
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import org.axonframework.axonserver.connector.AxonServerConfiguration
import org.axonframework.axonserver.connector.AxonServerConnectionManager
import org.axonframework.axonserver.connector.command.AxonServerCommandBus
import org.axonframework.commandhandling.SimpleCommandBus
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.Configuration
import org.axonframework.config.DefaultConfigurer
import org.axonframework.modelling.command.Repository
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer


@Startup
class Configuration @Inject constructor(val foodCardProjector: FoodCartProjector){

    private lateinit var config: Configuration

    private fun jacksonSerializer():Serializer = JacksonSerializer.builder().objectMapper(jacksonObjectMapper()).build()

    @PostConstruct
    fun initialize(){
        val axonServerConfiguration = AxonServerConfiguration.builder().servers("localhost:8124").build()

        val axonServerConnectionManager = AxonServerConnectionManager
            .builder()
            .axonServerConfiguration(axonServerConfiguration).build()

        val axonServerCommandBus = AxonServerCommandBus.builder()
            .axonServerConnectionManager(axonServerConnectionManager)
            .configuration(axonServerConfiguration)
            .localSegment(SimpleCommandBus.builder().build())
            .routingStrategy (AnnotationRoutingStrategy.defaultStrategy())
            .serializer(jacksonSerializer()).build()



        config =  DefaultConfigurer.defaultConfiguration()
            .configureCommandBus{
                axonServerCommandBus
            }
            .configureSerializer{
                jacksonSerializer()
            }
            .configureAggregate(FoodCart::class.java)
            .registerQueryHandler{
                foodCardProjector
            }
            .registerEventHandler{
                foodCardProjector
            }
            .buildConfiguration()

        config.start()
    }

    @Produces
    @ApplicationScoped
    fun commandGateway():CommandGateway = config.commandGateway()

    @Produces
    @ApplicationScoped
    fun queryGateway():QueryGateway = config.queryGateway()


    @Produces
    @ApplicationScoped
    fun foodCartRepository():Repository<FoodCart> = config.repository(FoodCart::class.java)
}

