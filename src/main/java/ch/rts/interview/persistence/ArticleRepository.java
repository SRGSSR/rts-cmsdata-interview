package ch.rts.interview.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

	/**
	 * Find articles by title containing the given text (case-insensitive)
	 */
	List<Article> findByTitleContainingIgnoreCase(String title);

	/**
	 * Find articles published after a specific date
	 */
	List<Article> findByPublicationDateAfter(LocalDateTime date);

	/**
	 * Find articles published between two dates, ordered by publication date descending
	 */
	List<Article> findByPublicationDateBetweenOrderByPublicationDateDesc(
			LocalDateTime startDate, 
			LocalDateTime endDate
	);

	/**
	 * Find articles containing a specific section
	 */
	@Query("SELECT a FROM Article a WHERE :section = ANY(a.sections)")
	List<Article> findBySection(@Param("section") String section);

	/**
	 * Find the most recent articles
	 */
	List<Article> findTop10ByOrderByPublicationDateDesc();
}

