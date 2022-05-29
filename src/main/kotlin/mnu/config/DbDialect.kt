package mnu.config

import org.hibernate.dialect.PostgreSQL95Dialect
import org.hibernate.type.StandardBasicTypes
import java.sql.Types

class DbDialect : PostgreSQL95Dialect() {
    init {
        registerHibernateType(Types.BIGINT, StandardBasicTypes.LONG.name)
    }
}