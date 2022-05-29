package mnu.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository

@NoRepositoryBean
interface BaseRepository<T, ID> : PagingAndSortingRepository<T, ID> {

    fun findOneById(id: ID): T?

    fun findAllByIdIsNotNull(pageable: Pageable): Page<T>

}