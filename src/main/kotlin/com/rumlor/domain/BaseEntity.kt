package com.rumlor.domain

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.*

@MappedSuperclass
open class BaseEntity(
    //why id can't be val?
    @Id open var id: UUID = UUID.randomUUID())