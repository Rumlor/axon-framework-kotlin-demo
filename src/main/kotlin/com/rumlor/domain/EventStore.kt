package com.rumlor.domain

import jakarta.persistence.Entity
import java.util.UUID

@Entity
class EventStore(eventIdentifier:UUID = UUID.randomUUID()):BaseEntity(eventIdentifier) {
}