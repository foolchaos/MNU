package mnu.model.entity

import java.io.Serializable
import org.hibernate.Hibernate
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class BaseEntity<T: Serializable>(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: T? = null
) {

    override fun toString(): String {
        return "${this::class.java.simpleName}(id=$id)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as BaseEntity<*>

        return id != null && id == other.id
    }

    override fun hashCode(): Int = 0

}