package com.rumlor.domain

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.*

@MappedSuperclass
open class BaseEntity {

    @Id
    //@GeneratedValue(strategy = GenerationType.UUID)
    protected open var id: UUID? = null

}